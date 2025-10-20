package org.example.web.ia;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

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

        Path root = Paths.get("").toAbsolutePath();
        Path entrada = root.resolve("entrada-usuario");
        Path patches = entrada.resolve("testes_ai_patches");
        Files.createDirectories(patches);

        int orcamentoTotal = envInt("IA_ORCAMENTO_CHARS", 22000);
        int orcamentoPorArquivo = envInt("IA_ORCAMENTO_POR_ARQUIVO", 1800);
        int maxArquivosPorSecao = envInt("IA_MAX_ARQUIVOS_SECAO", 12);

        String prompt = buildPromptComOrcamento(entrada, root, orcamentoTotal, orcamentoPorArquivo, maxArquivosPorSecao);

        GroqClient groq = new GroqClient(apiKey);

        JsonNode result = null;
        try {
            result = groq.chatJSON(modelo, systemRules(), prompt);
        } catch (IOException ex) {
            if (isTamanhoExcedido(ex)) {
                int reduzidoTotal = Math.max(8000, (int)(orcamentoTotal * 0.5));
                int reduzidoArquivo = Math.max(600, (int)(orcamentoPorArquivo * 0.5));
                String promptReduzido = buildPromptComOrcamento(entrada, root, reduzidoTotal, reduzidoArquivo, Math.max(6, maxArquivosPorSecao/2));
                try {
                    Thread.sleep(1200);
                } catch (InterruptedException ignored) {}
                result = groq.chatJSON(modelo, systemRules(), promptReduzido);
            } else {
                throw ex;
            }
        }

        int escritos = writeFilesFromJSON(result, patches);
        System.out.println("[IA] Arquivos de teste escritos: " + escritos);
        if (escritos == 0) {
            System.err.println("[IA] Nenhum patch gerado.");
            System.exit(3);
        }
    }

    private static boolean isTamanhoExcedido(IOException ex) {
        String s = String.valueOf(ex.getMessage());
        return s.contains("Request too large") || s.contains("TPM") || s.contains("413");
    }

    private static int envInt(String k, int def) {
        try { return Integer.parseInt(System.getenv().getOrDefault(k, String.valueOf(def))); }
        catch (Exception e) { return def; }
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
            "Você cria ou edita SOMENTE arquivos de TESTE (JUnit 5) no diretório entrada-usuario/testes_ai_patches.",
            "Não altere o código de produção.",
            "Ordem de precedência: 1) testes do usuário; 2) seus patches; 3) testes de explicações.",
            "Objetivo: compilar e rodar ao menos 5 testes cobrindo o código do usuário, alinhando nomes e pacotes.",
            "Use org.junit.jupiter.api.Test e Assertions estáticas.",
            "Responda em JSON: { \"files\": [ {\"path\": \"NomeTest.java\", \"content\": \"<codigo>\"}, ... ] }.",
            "Grave somente arquivos .java válidos."
        );
    }

    private static String buildPromptComOrcamento(Path entrada, Path root, int orcamentoTotal, int orcamentoPorArquivo, int maxArquivosPorSecao) throws IOException {
        StringBuilder sb = new StringBuilder();
        Budget b = new Budget(orcamentoTotal);

        b.append(sb, "== FONTES (staged) ==\n");
        b.append(sb, slurpDirLimit(root.resolve("build/usuario-src/src/main/java"), orcamentoPorArquivo, maxArquivosPorSecao));

        b.append(sb, "\n== TESTES RAW (stage) ==\n");
        b.append(sb, slurpDirLimit(root.resolve("build/usuario-src/src/test_raw/java"), orcamentoPorArquivo, maxArquivosPorSecao));

        b.append(sb, "\n== TESTES STAGE ==\n");
        b.append(sb, slurpDirLimit(root.resolve("build/usuario-src/src/test/java"), orcamentoPorArquivo, maxArquivosPorSecao));

        b.append(sb, "\n== TESTES DO USUÁRIO ==\n");
        b.append(sb, slurpDirLimit(entrada.resolve("testes"), orcamentoPorArquivo, maxArquivosPorSecao));

        b.append(sb, "\n== PATCHES EXISTENTES DA IA ==\n");
        b.append(sb, slurpDirLimit(entrada.resolve("testes_ai_patches"), orcamentoPorArquivo, maxArquivosPorSecao));

        b.append(sb, "\n== TESTES IA (explicações) ==\n");
        b.append(sb, slurpDirLimit(entrada.resolve("testes_explicações/tests"), orcamentoPorArquivo, Math.max(4, maxArquivosPorSecao/2)));

        b.append(sb, "\n== RESUMO JACOCO ==\n");
        b.append(sb, resumoJacoco(entrada.resolve("jacoco-relatorio/jacoco.xml"), 1500));

        b.append(sb, "\n== INSTRUÇÕES ==\n");
        b.append(sb, "Crie/edite somente testes JUnit 5. Não reescreva produção. Use nomes e pacotes existentes. Gere arquivos com nomes *Test.java.\n");

        String out = sb.toString();
        if (out.length() > orcamentoTotal) {
            out = out.substring(0, orcamentoTotal);
        }
        return out;
    }

    private static String slurpDirLimit(Path dir, int perFileChars, int maxFiles) throws IOException {
        if (!Files.isDirectory(dir)) return "(vazio: " + dir + ")";
        List<Path> files = Files.walk(dir)
                .filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".java"))
                .sorted()
                .limit(maxFiles)
                .collect(Collectors.toList());
        if (files.isEmpty()) return "(sem .java em " + dir + ")";
        StringBuilder sb = new StringBuilder();
        for (Path p : files) {
            String txt = Files.readString(p, StandardCharsets.UTF_8);
            if (txt.length() > perFileChars) {
                txt = txt.substring(0, perFileChars) + "\n/* ... TRUNCADO ... */";
            }
            sb.append("\n--- ").append(dir.relativize(p)).append(" ---\n").append(txt);
        }
        return sb.toString();
    }

    private static String resumoJacoco(Path xml, int maxChars) throws IOException {
        if (!Files.exists(xml)) return "(jacoco ausente)";
        String raw = Files.readString(xml, StandardCharsets.UTF_8);
        raw = raw.replaceAll("(?is)<!DOCTYPE[^>]*>", "");
        long missed = 0, covered = 0;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("<counter\\s+type=\"LINE\"\\s+missed=\"(\\d+)\"\\s+covered=\"(\\d+)\"").matcher(raw);
        while (m.find()) { missed += Long.parseLong(m.group(1)); covered += Long.parseLong(m.group(2)); }
        long total = missed + covered;
        String pct = (total == 0) ? "0.00" : String.format(java.util.Locale.US, "%.2f", (covered * 100.0) / total);
        String resumo = "linhas_total=" + total + ", linhas_cobertas=" + covered + ", linhas_perdidas=" + missed + ", cobertura=" + pct + "%";
        if (resumo.length() > maxChars) resumo = resumo.substring(0, maxChars);
        return resumo;
    }

    private static int writeFilesFromJSON(JsonNode json, Path outDir) throws IOException {
        if (json == null || !json.has("files") || !json.get("files").isArray()) return 0;
        int n = 0;
        for (JsonNode f : json.get("files")) {
            String path = optText(f, "path");
            String content = optText(f, "content");
            if (path == null || !path.endsWith(".java") || content == null || content.isBlank()) continue;
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

    private static final class Budget {
        private int remaining;
        Budget(int total) { this.remaining = Math.max(0, total); }
        void append(StringBuilder sb, String chunk) {
            if (chunk == null || chunk.isEmpty() || remaining <= 0) return;
            int take = Math.min(remaining, chunk.length());
            sb.append(chunk, 0, take);
            remaining -= take;
        }
    }
}
