package org.example.web.service;

import org.example.web.ia.ClienteIa;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.example.util.LeitorCodigo;
import org.example.util.Prompts;
import org.example.util.SonarUtil;

import java.nio.file.*;
import java.util.*;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GeradorTestesService {
    private final ClienteIa ia;

    @Value("${app.entrada.diretorio:entrada-usuario}")
    private String dirEntrada;

    // limites iniciais (adaptáveis em caso de 413)
    private static final int INIT_MAX_CODE_CHARS  = 5000;
    private static final int INIT_MAX_SONAR_CHARS = 3500;

    // pausa base entre chamadas (configurável por env IA_SLEEP_MS)
    private final long sleepBetweenCallsMs = Math.max(500L, readLongEnv("IA_SLEEP_MS", 1500L));

    public GeradorTestesService(ClienteIa ia) { this.ia = ia; }

    public Path gerarParaTodosArquivos(String sonarJsonPath) {
        try {
            Path baseEntrada = Path.of(dirEntrada);
            if (!baseEntrada.isAbsolute()) {
                Path cand = Path.of("app").resolve(dirEntrada);
                baseEntrada = Files.exists(cand) ? cand : baseEntrada;
            }
            Path base = baseEntrada.resolve("testes_explicações");
            Path pastaTests = base.resolve("tests");
            Path pastaExp = base.resolve("explicacoes");
            Files.createDirectories(pastaTests);
            Files.createDirectories(pastaExp);

            String sonarJson = "";
            try { sonarJson = Files.readString(Path.of(sonarJsonPath)); } catch (Exception ignored) {}

            List<Path> arquivos = listarJava(baseEntrada);
            Collections.sort(arquivos); // determinístico
            List<String> linhasRelatorio = new ArrayList<>();

            for (Path arq : arquivos) {
                String fileName = arq.getFileName().toString();
                int maxCode = INIT_MAX_CODE_CHARS;
                int maxSonar = INIT_MAX_SONAR_CHARS;

                boolean gerouAlgo = false;
                String erroFinal = null;

                for (int tentativa = 1; tentativa <= 3; tentativa++) {
                    try {
                        String codigo = LeitorCodigo.lerAteLimite(arq, maxCode);
                        String sonarCut = SonarUtil.extrairTrechoPorArquivo(sonarJson, fileName, maxSonar);
                        String prompt = Prompts.montarPromptGroqPorArquivo(sonarCut, arq.toString(), codigo);

                        var resp = ia.gerar(prompt);
                        Map<String,String> arquivosGerados = separarArquivos(resp.codigo());

                        if (arquivosGerados.isEmpty()) {
                            // fallback: gera esqueleto local
                            String skeleton = gerarEsqueleto(arq, codigo);
                            String nomeNorm = normalizarCaminho(nomeTestePorArquivo(arq));
                            Path alvo = pastaTests.resolve(nomeNorm);
                            Files.createDirectories(alvo.getParent());
                            Files.writeString(alvo, skeleton);
                            linhasRelatorio.add("SKELETON: " + fileName + " -> " + alvo);
                            gerouAlgo = true;
                        } else {
                            for (var e : arquivosGerados.entrySet()) {
                                String nomeNorm = normalizarCaminho(e.getKey());
                                Path alvo = pastaTests.resolve(nomeNorm);
                                Files.createDirectories(alvo.getParent());
                                String conteudo = ajustarPacote(e.getValue(), "org.example.generated");
                                Files.writeString(alvo, conteudo);
                                linhasRelatorio.add("OK: " + fileName + " -> " + alvo);
                                gerouAlgo = true;
                            }
                        }
                        if (resp.explicacao() != null && !resp.explicacao().isBlank()) {
                            Path exp = pastaExp.resolve("explicacoes.jsonl");
                            Files.writeString(exp, resp.explicacao() + System.lineSeparator(),
                                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                        }

                        // sucesso, pode sair do laço de tentativas
                        break;
                    } catch (RuntimeException ex) {
                        String msg = ex.getMessage() == null ? "" : ex.getMessage();

                        // Se for 413 (request too large) — reduz os limites e tenta de novo
                        if (msg.contains("413") || msg.toLowerCase().contains("too large")) {
                            maxCode = Math.max(1500, (int)(maxCode * 0.6));
                            maxSonar = Math.max(1000, (int)(maxSonar * 0.6));
                            erroFinal = "413 adaptado: novos limites code=" + maxCode + " sonar=" + maxSonar;
                            dormir( 600L );
                            continue;
                        }

                        // 429 deve ser tratado no cliente com backoff, mas se escapar aqui, dá uma pausa
                        if (msg.contains("429")) {
                            long esperar = sugerirEsperaMs(msg);
                            dormir(esperar);
                            erroFinal = "429 retry após " + esperar + "ms";
                            continue;
                        }

                        // Outros erros — guarda e tenta próxima tentativa (pouco backoff)
                        erroFinal = msg;
                        dormir( 400L );
                    }
                }

                if (!gerouAlgo) {
                    linhasRelatorio.add("ERRO: " + fileName + " -> " + (erroFinal == null ? "desconhecido" : erroFinal));
                }

                // pausa base entre arquivos para não estourar TPM
                dormir(sleepBetweenCallsMs);
            }

            Path resumo = base.resolve("relatorio.txt");
            Files.writeString(resumo, String.join(System.lineSeparator(), linhasRelatorio) + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            return base;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Path> listarJava(Path baseEntrada) throws IOException {
        List<Path> lista = new ArrayList<>();
        try (var walk = Files.walk(baseEntrada, 1)) { // nível 1 porque entrada foi achatada
            walk.filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".java"))
                .forEach(lista::add);
        }
        return lista;
    }

    private Map<String,String> separarArquivos(String bloco) {
        var mapa = new LinkedHashMap<String,String>();
        if (bloco == null) return mapa;
        var linhas = bloco.split("\n");
        String nome = null;
        var b = new StringBuilder();
        for (var l : linhas) {
            if (l.startsWith("<arquivo:")) {
                if (nome != null && b.length() > 0) mapa.put(nome, b.toString().trim());
                nome = l.substring(9, l.length()-1).trim();
                b.setLength(0);
            } else {
                b.append(l).append('\n');
            }
        }
        if (nome != null && b.length() > 0) mapa.put(nome, b.toString().trim());
        return mapa;
    }

    private String normalizarCaminho(String nome) {
        if (nome == null || nome.isBlank()) {
            return "org/example/generated/ArquivoGeradoTest.java";
        }
        String s = nome.trim().replace('\\','/');
        if (!s.contains("/")) s = s.replace('.', '/');
        s = s.replaceAll("/{2,}", "/");
        while (s.startsWith("/")) s = s.substring(1);
        if (s.endsWith("/java.java")) {
            int idx = s.lastIndexOf('/', s.length() - "/java.java".length() - 1);
            if (idx >= 0) {
                String className = s.substring(idx + 1, s.length() - "/java.java".length());
                s = s.substring(0, idx + 1) + className + ".java";
            } else {
                s = s.substring(0, s.length() - "/java.java".length()) + ".java";
            }
        }
        if (!s.endsWith(".java")) s = s + ".java";
        String file = s.substring(s.lastIndexOf('/') + 1);
        return "org/example/generated/" + file;
    }

    private String ajustarPacote(String conteudoOriginal, String pacoteDesejado) {
        if (conteudoOriginal == null) conteudoOriginal = "";
        String s = conteudoOriginal.stripLeading();
        if (!s.startsWith("package ")) {
            return "package " + pacoteDesejado + ";\n\n" + conteudoOriginal.trim() + "\n";
        }
        return s.replaceFirst("^package\\s+[^;]+;", "package " + pacoteDesejado + ";");
    }

    private String gerarEsqueleto(Path arquivoAlvo, String codigoAlvo) {
        String nomeBase = arquivoAlvo.getFileName().toString().replaceAll("\\.java$","");
        String classe = extrairNomeClasse(codigoAlvo);
        if (classe == null || classe.isBlank()) classe = nomeBase;
        String testName = classe.replaceAll("[^A-Za-z0-9_]", "") + "Test";
        return """
                package org.example.generated;

                import org.junit.jupiter.api.Test;
                import static org.junit.jupiter.api.Assertions.*;

                public class %s {
                    @Test
                    void deveCompilar() {
                        assertTrue(true);
                    }
                }
                """.formatted(testName);
    }

    private String nomeTestePorArquivo(Path arquivoAlvo) {
        String base = arquivoAlvo.getFileName().toString().replaceAll("\\.java$","");
        return "org.example.generated." + base + "Test.java";
    }

    private static final Pattern CLASS_NAME = Pattern.compile("\\bclass\\s+([A-Za-z_][A-Za-z0-9_]*)");
    private String extrairNomeClasse(String codigo) {
        if (codigo == null) return null;
        Matcher m = CLASS_NAME.matcher(codigo);
        if (m.find()) return m.group(1);
        return null;
    }

    private static long readLongEnv(String name, long def) {
        try {
            String v = System.getenv(name);
            if (v == null || v.isBlank()) return def;
            return Long.parseLong(v.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private static long sugerirEsperaMs(String msg) {
        try {
            var p = java.util.regex.Pattern.compile("try again in\\s+([0-9]+(?:\\.[0-9]+)?)s", java.util.regex.Pattern.CASE_INSENSITIVE);
            var m = p.matcher(msg == null ? "" : msg);
            if (m.find()) {
                double s = Double.parseDouble(m.group(1));
                return (long)(s * 1000.0) + 400L;
            }
        } catch (Exception ignored) {}
        return 10_000L;
    }

    private static void dormir(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
