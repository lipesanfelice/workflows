package org.example.web.ia;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CLI que lê fontes/testes/relatórios do workspace e pede para a Groq gerar/ajustar
 * SOMENTE arquivos de teste (JUnit 5), gravando em entrada-usuario/testes_ai_patches.
 *
 * Uso via Gradle task: ./gradlew -p app ajustarTestesIa
 * ou via bootRun:       ./gradlew -p app bootRun --args='--acao=ajustar-testes ...'
 */
public class AjustarTestesCli {

    private static final ObjectMapper M = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        Map<String,String> cli = parseArgs(args);
        String modelo = cli.getOrDefault("modelo", System.getenv().getOrDefault("GROQ_MODELO", "llama-3.1-8b-instant"));
        String apiKey = System.getenv("GROQ_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("GROQ_API_KEY ausente. Abortando ajuste automático.");
            System.exit(2);
        }

        // ✅ Raiz = diretório atual do módulo :app (JavaExec roda com workdir = :app)
        Path root = Paths.get("").toAbsolutePath();
        Path entrada = root.resolve("entrada-usuario");
        Path patches = entrada.resolve("testes_ai_patches");
        Files.createDirectories(patches);

        // Monte o contexto (curto e objetivo)
        String prompt = buildPrompt(entrada, root);

        // Chama a Groq pedindo um JSON de saída { "files":[ {"path":"FooTest.java","content":"..."} ] }
        GroqClient groq = new GroqClient(apiKey);
        JsonNode result = groq.chatJSON(modelo, systemRules(), prompt);

        int escritos = writeFilesFromJSON(result, patches);
        System.out.println("[IA] Arquivos de teste escritos: " + escritos);
        if (escritos == 0) {
            System.err.println("[IA] Nenhum patch gerado.");
            System.exit(3);
        }
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String,String> map = new HashMap<>();
        for (String a : args) {
            if (a.startsWith("--") && a.contains("=")) {
                int i = a.indexOf('=');
                map.put(a.substring(2, i), a.substring(i+1));
            }
        }
        return map;
    }

    private static String systemRules() {
        return String.join("\n",
            "Você é uma ferramenta que SOMENTE cria/edita TESTES (JUnit 5).",
            "NÃO altere o código de produção; ele é apenas contexto.",
            "Ordem de precedência dos testes no build:",
            "1) entrada-usuario/testes (usuário) — NÃO modifique nem sobrescreva.",
            "2) entrada-usuario/testes_ai_patches (você grava aqui).",
            "3) entrada-usuario/testes_explicações/tests (histórico/apoio).",
            "",
            "Objetivo:",
            "- Consertar erros de compilação/descoberta e alinhar nomes de classes/pacotes usados nos testes.",
            "- Garantir que pelo menos 5 testes executem o código do usuário (não apenas main interativo).",
            "- Se havia divergência de nome (ex.: GerenciadorTarefas -> Tarefas), ajuste os testes.",
            "- Use JUnit 5 (org.junit.jupiter.api.Test) e Assertions estáticas.",
            "- Se necessário, crie testes 'smoke' mínimos por classe pública.",
            "",
            "Saída OBRIGATÓRIA em JSON: { \"files\": [ {\"path\": \"NomeTest.java\", \"content\": \"<código Java completo>\"}, ... ] }",
            "Todos os arquivos devem ser gravados em entrada-usuario/testes_ai_patches/."
        );
    }

    private static String buildPrompt(Path entrada, Path root) throws IOException {
        List<String> parts = new ArrayList<>();

        parts.add("== FONTES DO USUÁRIO (staged) ==");
        parts.add(slurpDir(root.resolve("build/usuario-src/src/main/java")));

        parts.add("== TESTES RAW (stage) ==");
        parts.add(slurpDir(root.resolve("build/usuario-src/src/test_raw/java")));

        parts.add("== TESTES STAGE (compiláveis) ==");
        parts.add(slurpDir(root.resolve("build/usuario-src/src/test/java")));

        parts.add("== TESTES DO USUÁRIO (originais) ==");
        parts.add(slurpDir(entrada.resolve("testes")));

        parts.add("== PATCHES EXISTENTES DA IA ==");
        parts.add(slurpDir(entrada.resolve("testes_ai_patches")));

        parts.add("== TESTES IA (explicações) ==");
        parts.add(slurpDir(entrada.resolve("testes_explicações/tests")));

        parts.add("== JACOCO ==");
        parts.add(slurpFile(entrada.resolve("jacoco-relatorio/jacoco.xml")));

        parts.add("== PROBLEMS REPORT ==");
        parts.add(slurpFile(root.resolve("build/reports/problems/problems-report.html")));

        return String.join("\n\n", parts);
    }

    private static String slurpDir(Path dir) throws IOException {
        if (!Files.isDirectory(dir)) return "(vazio: " + dir + ")";
        List<Path> files = Files.walk(dir)
                .filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".java"))
                .sorted()
                .collect(Collectors.toList());
        if (files.isEmpty()) return "(sem .java em " + dir + ")";
        StringBuilder sb = new StringBuilder();
        for (Path p : files) {
            String txt = Files.readString(p, StandardCharsets.UTF_8);
            if (txt.length() > 120_000) {
                txt = txt.substring(0, 120_000) + "\n/* ... TRUNCATED ... */";
            }
            sb.append("\n--- ").append(dir.relativize(p)).append(" ---\n").append(txt);
        }
        return sb.toString();
    }

    private static String slurpFile(Path file) throws IOException {
        if (!Files.exists(file)) return "(ausente: " + file + ")";
        String txt = Files.readString(file, StandardCharsets.UTF_8);
        if (txt.length() > 200_000) {
            txt = txt.substring(0, 200_000) + "\n<!-- ... TRUNCATED ... -->";
        }
        return "\n--- " + file.getFileName() + " ---\n" + txt;
    }

    private static int writeFilesFromJSON(JsonNode json, Path outDir) throws IOException {
        if (json == null || !json.has("files") || !json.get("files").isArray()) return 0;
        int n = 0;
        for (JsonNode f : json.get("files")) {
            String path = optText(f, "path");
            String content = optText(f, "content");
            if (path == null || !path.endsWith(".java") || content == null || content.isBlank()) continue;
            // grava SEM respeitar subpastas para evitar sobrescrever testes do usuário
            Path dest = outDir.resolve(new File(path).getName());
            Files.writeString(dest, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("[IA] escrito: " + dest);
            n++;
        }
        return n;
    }

    private static String optText(JsonNode n, String field) {
        return (n != null && n.hasNonNull(field)) ? n.get(field).asText() : null;
    }
}
