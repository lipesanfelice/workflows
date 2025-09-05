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
import java.util.regex.*;

@Service
public class GeradorTestesService {
    private final ClienteIa ia;

    @Value("${app.entrada.diretorio:entrada-usuario}")
    private String dirEntrada;

    // limites iniciais (reduzimos para caber folgado no tier grátis)
    private final int INIT_MAX_CODE_CHARS  = (int) readLongEnv("IA_CODE_MAX_CHARS", 3000);
    private final int INIT_MAX_SONAR_CHARS = (int) readLongEnv("IA_SONAR_MAX_CHARS", 2000);
    private final long sleepBetweenCallsMs = Math.max(1000L, readLongEnv("IA_SLEEP_MS", 7000L)); // 7s por arquivo

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

            String sonarJson = ""; try { sonarJson = Files.readString(Path.of(sonarJsonPath)); } catch (Exception ignored) {}
            List<Path> arquivos = listarJava(baseEntrada);
            Collections.sort(arquivos);

            List<String> rel = new ArrayList<>();
            for (Path arq : arquivos) {
                String fileName = arq.getFileName().toString();
                int maxCode = INIT_MAX_CODE_CHARS, maxSonar = INIT_MAX_SONAR_CHARS;
                boolean gerou = false; String ultimoErro = null;

                for (int tent = 1; tent <= 3; tent++) {
                    try {
                        String codigo = LeitorCodigo.lerAteLimite(arq, maxCode);
                        String sonarCut = SonarUtil.extrairTrechoPorArquivo(sonarJson, fileName, maxSonar);
                        String prompt = Prompts.montarPromptGroqPorArquivo(sonarCut, arq.toString(), codigo);

                        var resp = ia.gerar(prompt);

                        Map<String,String> arquivosGerados = separarArquivos(resp.codigo());
                        if (arquivosGerados.isEmpty()) {
                            // Fallback 1: se veio "código cru" (sem tags), usa-o como arquivo único.
                            String cru = resp.codigo();
                            if (cru != null && !cru.isBlank()) {
                                String nomeNorm = normalizarCaminho(nomeTestePorArquivo(arq));
                                Path alvo = pastaTests.resolve(nomeNorm);
                                Files.createDirectories(alvo.getParent());
                                Files.writeString(alvo, ajustarPacote(cru, "org.example.generated"));
                                rel.add("RAW_OK: " + fileName + " -> " + alvo);
                                gerou = true;
                            } else {
                                // Fallback 2: esqueleto mínimo
                                String skeleton = gerarEsqueleto(arq, codigo);
                                String nomeNorm = normalizarCaminho(nomeTestePorArquivo(arq));
                                Path alvo = pastaTests.resolve(nomeNorm);
                                Files.createDirectories(alvo.getParent());
                                Files.writeString(alvo, skeleton);
                                rel.add("SKELETON: " + fileName + " -> " + alvo);
                                gerou = true;
                            }
                        } else {
                            for (var e : arquivosGerados.entrySet()) {
                                String nomeNorm = normalizarCaminho(e.getKey());
                                Path alvo = pastaTests.resolve(nomeNorm);
                                Files.createDirectories(alvo.getParent());
                                Files.writeString(alvo, ajustarPacote(e.getValue(), "org.example.generated"));
                                rel.add("OK: " + fileName + " -> " + alvo);
                                gerou = true;
                            }
                        }

                        if (resp.explicacao() != null && !resp.explicacao().isBlank()) {
                            Path exp = pastaExp.resolve("explicacoes.jsonl");
                            Files.writeString(exp, resp.explicacao() + System.lineSeparator(),
                                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                        }
                        break; // sucesso, sai das tentativas
                    } catch (RuntimeException ex) {
                        String msg = ex.getMessage() == null ? "" : ex.getMessage();
                        // 413 => reduz e tenta de novo
                        if (msg.contains("413") || msg.toLowerCase().contains("too large")) {
                            maxCode = Math.max(1200, (int)(maxCode * 0.6));
                            maxSonar = Math.max(800,  (int)(maxSonar * 0.6));
                            ultimoErro = "413 adaptado para code=" + maxCode + " sonar=" + maxSonar;
                            dormir(600L); continue;
                        }
                        // 429 — ClienteIa já tenta backoff; se sobrar erro, dá pequena pausa e tenta
                        if (msg.contains("429")) { ultimoErro = "429"; dormir(2000L); continue; }
                        ultimoErro = msg; dormir(400L);
                    }
                }

                if (!gerou) rel.add("ERRO: " + fileName + " -> " + (ultimoErro==null?"desconhecido":ultimoErro));
                dormir(sleepBetweenCallsMs); // intervala entre arquivos para não estourar TPM
            }

            Path resumo = base.resolve("relatorio.txt");
            Files.writeString(resumo, String.join(System.lineSeparator(), rel) + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            return base;
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private List<Path> listarJava(Path baseEntrada) throws IOException {
        List<Path> lista = new ArrayList<>();
        try (var walk = Files.walk(baseEntrada, 1)) {
            walk.filter(Files::isRegularFile).filter(p -> p.toString().endsWith(".java")).forEach(lista::add);
        }
        return lista;
    }

    private Map<String,String> separarArquivos(String bloco) {
        var mapa = new LinkedHashMap<String,String>();
        if (bloco == null) return mapa;
        var linhas = bloco.split("\n");
        String nome = null; var b = new StringBuilder();
        for (var l : linhas) {
            if (l.startsWith("<arquivo:")) {
                if (nome != null && b.length() > 0) mapa.put(nome, b.toString().trim());
                nome = l.substring(9, l.length()-1).trim(); b.setLength(0);
            } else { b.append(l).append('\n'); }
        }
        if (nome != null && b.length() > 0) mapa.put(nome, b.toString().trim());
        return mapa;
    }

    private String normalizarCaminho(String nome) {
        if (nome == null || nome.isBlank()) return "org/example/generated/ArquivoGeradoTest.java";
        String s = nome.trim().replace('\\','/');
        if (!s.contains("/")) s = s.replace('.', '/');
        s = s.replaceAll("/{2,}", "/");
        while (s.startsWith("/")) s = s.substring(1);
        if (s.endsWith("/java.java")) {
            int idx = s.lastIndexOf('/', s.length() - "/java.java".length() - 1);
            s = (idx >= 0) ? s.substring(0, idx + 1) + s.substring(idx + 1, s.length() - "/java.java".length()) + ".java"
                           : s.substring(0, s.length() - "/java.java".length()) + ".java";
        }
        if (!s.endsWith(".java")) s = s + ".java";
        String file = s.substring(s.lastIndexOf('/') + 1);
        return "org/example/generated/" + file;
    }

    private String ajustarPacote(String conteudoOriginal, String pacoteDesejado) {
        if (conteudoOriginal == null) conteudoOriginal = "";
        String s = conteudoOriginal.stripLeading();
        if (!s.startsWith("package ")) return "package " + pacoteDesejado + ";\n\n" + conteudoOriginal.trim() + "\n";
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
        var m = CLASS_NAME.matcher(codigo);
        return m.find() ? m.group(1) : null;
    }

    private static long readLongEnv(String name, long def){
        try{ var v=System.getenv(name); return (v==null||v.isBlank())?def:Long.parseLong(v.trim()); }catch(Exception e){ return def; }
    }
    private static void dormir(long ms){ try{ Thread.sleep(ms);}catch(InterruptedException ignored){} }
}
