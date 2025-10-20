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
 * CLI que pede à Groq correções de TESTES (JUnit 5) e, opcionalmente,
 * um patch MINIMAMENTE INVASIVO no código de produção para destravar compilação.
 *
 * Modos:
 *  - --modo=ajustar-testes  (default): gera/arruma SOMENTE testes.
 *  - --modo=fix_main        : permite patches mínimos em produção (kind=main) p/ compilar.
 *
 * Uso: ./gradlew -p app ajustarTestesIa --modelo=llama-3.1-8b-instant --modo=ajustar-testes
 */
public class AjustarTestesCli {

    private static final ObjectMapper M = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        Map<String,String> cli = parseArgs(args);
        String modelo = cli.getOrDefault("modelo", System.getenv().getOrDefault("GROQ_MODELO", "llama-3.1-8b-instant"));
        String modo   = cli.getOrDefault("modo", "ajustar-testes").trim();
        String apiKey = System.getenv("GROQ_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("GROQ_API_KEY ausente. Abortando ajuste automático.");
            System.exit(2);
        }

        // Raiz = diretório atual do módulo :app
        Path root    = Paths.get("").toAbsolutePath();
        Path entrada = root.resolve("entrada-usuario");
        Path patches = entrada.resolve("testes_ai_patches");
        Files.createDirectories(patches);

        // Meta detectada pelo Gradle (package/classe pública, se houver)
        MetaInfo meta = readMeta(entrada.resolve("meta/nome_classe.env"));

        // Prompts
        String sys = systemRules(modo, meta);
        String prompt = buildPrompt(entrada, root, meta, modo);

        // Chamada à Groq
        GroqClient groq = new GroqClient(apiKey);
        JsonNode result = groq.chatJSON(modelo, sys, prompt);

        // Escrita
        WriteStats stats = writeFilesFromJSON(result, patches, entrada, modo);
        System.out.println("[IA] Arquivos escritos: tests=" + stats.testsWritten + ", main=" + stats.mainWritten);

        if (stats.testsWritten == 0 && !"fix_main".equals(modo)) {
            System.err.println("[IA] Nenhum patch de teste gerado.");
            System.exit(3);
        }
    }

    /* ============================ PROMPTS ============================ */

    private static String systemRules(String modo, MetaInfo meta) {
        List<String> rules = new ArrayList<>();

        rules.add("Você é uma ferramenta que trabalha com projeto Java 21.");
        if ("fix_main".equals(modo)) {
            rules.add("MODO: FIX_MAIN — você PODE propor patches mínimos em produção (kind=\"main\") EM ADIÇÃO a testes.");
            rules.add("Regras para patches em produção:");
            rules.add("- Objetivo é APENAS permitir COMPILAR: correções sintáticas, fechamento de chaves, remoção de lixo fora da classe, adicionar/ajustar package, pequenos stubs internos.");
            rules.add("- NÃO altere comportamento/semântica: não mude assinaturas públicas, não troque nomes públicos, não implemente lógica de negócio nova.");
            rules.add("- Preserve nomes/pacote existentes. Se inexistentes, inferir a partir dos arquivos staged e de " +
                      "USUARIO_PACKAGE=" + (meta.pkg == null ? "" : meta.pkg) + " e USUARIO_PUBLIC_CLASS=" + (meta.publicClass == null ? "" : meta.publicClass) + ".");
            rules.add("- As alterações de produção devem ser entregues com JSON {\"files\":[{\"path\":\"<caminho relativo dentro de entrada-usuario>\",\"content\":\"...\",\"kind\":\"main\"}]}.");
        } else {
            rules.add("MODO: AJUSTAR-TESTES — você SOMENTE cria/edita TESTES (JUnit 5). Não edite produção.");
        }

        rules.add("");
        rules.add("Ordem de precedência de testes no build (NÃO sobrescreva os do usuário):");
        rules.add("1) entrada-usuario/testes (usuário) — NÃO modifique nem sobrescreva.");
        rules.add("2) entrada-usuario/testes_ai_patches (você grava aqui).");
        rules.add("3) entrada-usuario/testes_explicações/tests (histórico/apoio).");

        rules.add("");
        rules.add("Políticas para TESTES:");
        rules.add("- Use JUnit 5 (org.junit.jupiter.api.Test) e Assertions estáticas (import static org.junit.jupiter.api.Assertions.*).");
        rules.add("- Não dependa de entrada interativa (System.in/Scanner loops). Não bloqueie esperando input.");
        rules.add("- Prefira testar métodos diretamente. Se necessário, use reflexão para atingir membros não públicos.");
        rules.add("- Se existir apenas um 'main' que lê input, trate-o como apenas um entrypoint; os testes não devem depender de um loop ou de interações sucessivas.");
        rules.add("- Alinhe pacote com o código de produção. Quando houver USUARIO_PACKAGE, prefixe os testes com esse package.");
        rules.add("- Se houver divergência de nomes, ajuste os TESTES (não mude produção), exceto no FIX_MAIN, onde apenas correções sintáticas são permitidas.");
        rules.add("- Gere pelo menos 1 teste 'smoke' por classe pública detectada, cobrindo pelo menos uma execução de método real.");
        rules.add("- Evite @Disabled, sleeps, timeouts desnecessários ou dependências externas.");
        rules.add("- Estruture nome de arquivos como <NomeDaClasse>Test.java.");
        rules.add("- Garanta que os testes sejam determinísticos.");

        rules.add("");
        rules.add("Cobertura (objetivo principal):");
        rules.add("- Leia o jacoco.xml e ataque métodos/linhas sem cobertura.");
        rules.add("- Se não houver jacoco.xml ou não houver linhas cobertas, gere smoke-tests mínimos por classe pública, com foco em caminhos simples e estáveis.");
        rules.add("- Nunca invente APIs não existentes; use reflexão quando necessário.");

        rules.add("");
        rules.add("Formato OBRIGATÓRIO de saída (JSON):");
        rules.add("{ \"files\": [");
        rules.add("  {\"path\": \"NomeTest.java\", \"content\": \"<codigo>\", \"kind\": \"test\"},");
        if ("fix_main".equals(modo)) {
            rules.add("  {\"path\": \"org/example/Foo.java\", \"content\": \"<codigo>\", \"kind\": \"main\"}");
        }
        rules.add("]}");
        rules.add("Para TESTES, sempre grave apenas o nome do arquivo em 'path' (sem subpastas). Para PRODUÇÃO (FIX_MAIN), 'path' deve ser relativo a 'entrada-usuario/'.");
        return String.join("\n", rules);
    }

    private static String buildPrompt(Path entrada, Path root, MetaInfo meta, String modo) throws IOException {
        List<String> parts = new ArrayList<>();

        parts.add("== CONTEXTO ==");
        parts.add("USUARIO_PACKAGE=" + (meta.pkg == null ? "" : meta.pkg));
        parts.add("USUARIO_PUBLIC_CLASS=" + (meta.publicClass == null ? "" : meta.publicClass));
        parts.add("MODO=" + modo);

        parts.add("\n== FONTES DO USUÁRIO (staged) ==");
        parts.add(slurpDir(root.resolve("build/usuario-src/src/main/java")));

        parts.add("\n== TESTES RAW (stage) ==");
        parts.add(slurpDir(root.resolve("build/usuario-src/src/test_raw/java")));

        parts.add("\n== TESTES STAGE (compiláveis) ==");
        parts.add(slurpDir(root.resolve("build/usuario-src/src/test/java")));

        parts.add("\n== TESTES DO USUÁRIO (originais) ==");
        parts.add(slurpDir(entrada.resolve("testes")));

        parts.add("\n== PATCHES EXISTENTES DA IA ==");
        parts.add(slurpDir(entrada.resolve("testes_ai_patches")));

        parts.add("\n== TESTES IA (explicações) ==");
        parts.add(slurpDir(entrada.resolve("testes_explicações/tests")));

        parts.add("\n== JACOCO ==");
        parts.add(slurpFile(entrada.resolve("jacoco-relatorio/jacoco.xml")));

        parts.add("\n== PROBLEMS REPORT ==");
        parts.add(slurpFile(root.resolve("build/reports/problems/problems-report.html")));

        parts.add("\n== INSTRUÇÕES FINAIS ==");
        parts.add("Produza o JSON exatamente no formato especificado, sem comentários fora do JSON.");

        return String.join("\n", parts);
    }

    /* ============================ ESCRITA ============================ */

    private static WriteStats writeFilesFromJSON(JsonNode json, Path outTestsDir, Path entradaDir, String modo) throws IOException {
        WriteStats stats = new WriteStats();
        if (json == null || !json.has("files") || !json.get("files").isArray()) return stats;

        for (JsonNode f : json.get("files")) {
            String path    = optText(f, "path");
            String content = optText(f, "content");
            String kind    = optText(f, "kind"); // "test" (default) | "main"

            if (content == null || content.isBlank()) continue;

            if (kind == null || kind.isBlank()) kind = "test";

            if ("main".equals(kind)) {
                if (!"fix_main".equals(modo)) {
                    System.out.println("[IA] Ignorando patch de produção (kind=main) fora do modo fix_main: " + path);
                    continue;
                }
                if (path == null || !path.endsWith(".java")) {
                    System.out.println("[IA] Patch main ignorado (path inválido): " + path);
                    continue;
                }
                // grava dentro de entrada-usuario/ preservando subpastas
                Path dest = entradaDir.resolve(path.replace("\\", "/"));
                Files.createDirectories(dest.getParent());
                Files.writeString(dest, normalizeJava(content), StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                System.out.println("[IA] escrito (main): " + entradaDir.relativize(dest));
                stats.mainWritten++;
            } else {
                // TESTES: sempre flatten do nome para evitar colisão com testes do usuário
                if (path == null || !path.endsWith(".java")) continue;
                Path dest = outTestsDir.resolve(new File(path).getName());
                Files.createDirectories(dest.getParent());
                Files.writeString(dest, normalizeJava(content), StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                System.out.println("[IA] escrito (test): " + dest.getFileName());
                stats.testsWritten++;
            }
        }
        return stats;
    }

    private static String normalizeJava(String s) {
        if (s == null) return "";
        // remove cercas markdown e BOM; evita lixo antes do package/class
        String out = s.replace("\uFEFF","")
                .replaceAll("(?m)^```\\w*\\s*\\r?\\n?", "")
                .replaceAll("(?m)^```\\s*$", "");
        return out;
    }

    /* ============================ IO HELPERS ============================ */

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

    private static String optText(JsonNode n, String field) {
        return (n != null && n.hasNonNull(field)) ? n.get(field).asText() : null;
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

    /* ============================ META ============================ */

    private static MetaInfo readMeta(Path envFile) {
        MetaInfo m = new MetaInfo();
        try {
            if (Files.exists(envFile)) {
                List<String> lines = Files.readAllLines(envFile, StandardCharsets.UTF_8);
                for (String ln : lines) {
                    String s = ln.trim();
                    if (s.startsWith("USUARIO_PACKAGE=")) {
                        m.pkg = s.substring("USUARIO_PACKAGE=".length()).trim();
                        if (m.pkg.isEmpty()) m.pkg = null;
                    } else if (s.startsWith("USUARIO_PUBLIC_CLASS=")) {
                        m.publicClass = s.substring("USUARIO_PUBLIC_CLASS=".length()).trim();
                        if (m.publicClass.isEmpty()) m.publicClass = null;
                    }
                }
            }
        } catch (Exception ignore) {}
        return m;
    }

    private static final class MetaInfo {
        String pkg;
        String publicClass;
    }

    private static final class WriteStats {
        int testsWritten;
        int mainWritten;
    }
}
