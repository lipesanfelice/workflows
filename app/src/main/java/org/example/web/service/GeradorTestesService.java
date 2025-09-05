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

    // limites (podem ser ajustados via env)
    private final int INIT_MAX_CODE_CHARS  = (int) readLongEnv("IA_CODE_MAX_CHARS", 3000);
    private final int INIT_MAX_SONAR_CHARS = (int) readLongEnv("IA_SONAR_MAX_CHARS", 2000);
    private final long sleepBetweenCallsMs = Math.max(1000L, readLongEnv("IA_SLEEP_MS", 7000L)); // pausa entre arquivos

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
                String baseName = fileName.replaceAll("\\.java$", "");
                String desiredFileName = baseName + "_testes.java";          // <<<<< NOME FORÇADO
                String desiredClassName = baseName + "_testes";              // <<<<< CLASSE FORÇADA

                int maxCode = INIT_MAX_CODE_CHARS, maxSonar = INIT_MAX_SONAR_CHARS;
                boolean gerou = false; String ultimoErro = null;

                for (int tent = 1; tent <= 3; tent++) {
                    try {
                        String codigo = LeitorCodigo.lerAteLimite(arq, maxCode);
                        String sonarCut = SonarUtil.extrairTrechoPorArquivo(sonarJson, fileName, maxSonar);
                        String prompt = Prompts.montarPromptGroqPorArquivo(sonarCut, arq.toString(), codigo);

                        var resp = ia.gerar(prompt);

                        // Ignora o nome que a IA sugerir e salva SEMPRE com <NomeOriginal>_testes.java
                        Map<String,String> arquivosGerados = separarArquivos(resp.codigo());
                        if (!arquivosGerados.isEmpty()) {
                            // usa o PRIMEIRO arquivo que vier (mas nomeamos como queremos)
                            String conteudo = arquivosGerados.values().iterator().next();
                            conteudo = ajustarPacote(conteudo, "org.example.generated");
                            conteudo = ajustarNomeClasseParaArquivo(conteudo, desiredClassName);
                            Path alvo = pastaTests.resolve(desiredFileName);
                            Files.createDirectories(alvo.getParent());
                            Files.writeString(alvo, conteudo);
                            rel.add("OK: " + fileName + " -> " + alvo.getFileName());
                            gerou = true;
                        } else {
                            // Fallback RAW: se a IA mandou algo sem tags, usa como código e padroniza nomes
                            String cru = resp.codigo();
                            if (cru != null && !cru.isBlank()) {
                                cru = ajustarPacote(cru, "org.example.generated");
                                cru = ajustarNomeClasseParaArquivo(cru, desiredClassName);
                                Path alvo = pastaTests.resolve(desiredFileName);
                                Files.createDirectories(alvo.getParent());
                                Files.writeString(alvo, cru);
                                rel.add("RAW_OK: " + fileName + " -> " + alvo.getFileName());
                                gerou = true;
                            } else {
                                // Fallback SKELETON
                                String skeleton = gerarEsqueleto(desiredClassName);
                                Path alvo = pastaTests.resolve(desiredFileName);
                                Files.createDirectories(alvo.getParent());
                                Files.writeString(alvo, skeleton);
                                rel.add("SKELETON: " + fileName + " -> " + alvo.getFileName());
                                gerou = true;
                            }
                        }

                        if (resp.explicacao() != null && !resp.explicacao().isBlank()) {
                            Path exp = pastaExp.resolve("explicacoes.jsonl");
                            Files.writeString(exp, resp.explicacao() + System.lineSeparator(),
                                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                        }
                        break; // sucesso
                    } catch (RuntimeException ex) {
                        String msg = ex.getMessage() == null ? "" : ex.getMessage();
                        if (msg.contains("413") || msg.toLowerCase().contains("too large")) {
                            maxCode = Math.max(1200, (int)(maxCode * 0.6));
                            maxSonar = Math.max(800,  (int)(maxSonar * 0.6));
                            ultimoErro = "413 adaptado code=" + maxCode + " sonar=" + maxSonar;
                            dormir(600L);
                            continue;
                        }
                        if (msg.contains("429")) { ultimoErro = "429"; dormir(2000L); continue; }
                        ultimoErro = msg; dormir(400L);
                    }
                }

                if (!gerou) rel.add("ERRO: " + fileName + " -> " + (ultimoErro==null?"desconhecido":ultimoErro));
                dormir(sleepBetweenCallsMs);
            }

            Path resumo = base.resolve("relatorio.txt");
            Files.writeString(resumo, String.join(System.lineSeparator(), rel) + System.lineSeparator(),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);


            return base;
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    // === util ===

    private List<Path> listarJava(Path baseEntrada) throws IOException {
        List<Path> lista = new ArrayList<>();
        try (var walk = Files.walk(baseEntrada, 1)) {
            walk.filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".java"))
                .forEach(lista::add);
        }
        return lista;
    }

    /** Divide bloco em <arquivo: ...> ... por arquivo (mas ignoramos o nome retornado). */
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

    private String ajustarPacote(String conteudoOriginal, String pacoteDesejado) {
        if (conteudoOriginal == null) conteudoOriginal = "";
        String s = conteudoOriginal.stripLeading();
        if (!s.startsWith("package ")) return "package " + pacoteDesejado + ";\n\n" + conteudoOriginal.trim() + "\n";
        return s.replaceFirst("^package\\s+[^;]+;", "package " + pacoteDesejado + ";");
    }

    // Força o nome da classe pública (ou primeira classe) a casar com o nome do arquivo desejado
    private String ajustarNomeClasseParaArquivo(String code, String desiredClassName) {
        if (code == null || code.isBlank()) return code;
        // public class / class / final class / abstract class ...
        Pattern p = Pattern.compile("(\\b(public\\s+)?(abstract\\s+|final\\s+)?class\\s+)([A-Za-z_][A-Za-z0-9_]*)");
        Matcher m = p.matcher(code);
        if (m.find()) {
            String prefix = m.group(1);
            return m.replaceFirst(Matcher.quoteReplacement(prefix + desiredClassName));
        }
        // se não achou classe, cria uma.
        return """
               package org.example.generated;

               public class %s {
                   @org.junit.jupiter.api.Test
                   void deveCompilar() { org.junit.jupiter.api.Assertions.assertTrue(true); }
               }
               """.formatted(desiredClassName);
    }

    private String gerarEsqueleto(String desiredClassName) {
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
                """.formatted(desiredClassName);
    }

    private static long readLongEnv(String name, long def){
        try{ var v=System.getenv(name); return (v==null||v.isBlank())?def:Long.parseLong(v.trim()); }catch(Exception e){ return def; }
    }
    private static void dormir(long ms){ try{ Thread.sleep(ms);}catch(InterruptedException ignored){} }
}
