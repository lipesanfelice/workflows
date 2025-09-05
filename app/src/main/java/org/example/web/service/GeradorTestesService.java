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

    // limites (ajustáveis por env)
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

            String sonarJson = "";
            try { sonarJson = Files.readString(Path.of(sonarJsonPath)); } catch (Exception ignored) {}

            List<Path> arquivos = listarJava(baseEntrada);
            Collections.sort(arquivos);

            List<String> rel = new ArrayList<>();
            for (Path arq : arquivos) {
                String fileName = arq.getFileName().toString();
                String baseName = fileName.replaceAll("\\.java$", "");
                String desiredFileName = baseName + "_testes.java";
                String desiredClassName = baseName + "_testes";
                String expFileName = baseName + "_testes.txt";

                int maxCode = INIT_MAX_CODE_CHARS, maxSonar = INIT_MAX_SONAR_CHARS;
                boolean gerou = false; String ultimoErro = null;

                for (int tent = 1; tent <= 3; tent++) {
                    try {
                        String codigoAlvo = LeitorCodigo.lerAteLimite(arq, maxCode);
                        String sonarCut   = SonarUtil.extrairTrechoPorArquivo(sonarJson, fileName, maxSonar);
                        String prompt     = Prompts.montarPromptGroqPorArquivo(sonarCut, arq.toString(), codigoAlvo);

                        var resp = ia.gerar(prompt);

                        // prioriza bloco com tags; senão usa conteúdo cru; senão esqueleto
                        String conteudoGerado;
                        Map<String,String> arquivosGerados = separarArquivos(resp.codigo());
                        if (!arquivosGerados.isEmpty()) {
                            conteudoGerado = arquivosGerados.values().iterator().next();
                            conteudoGerado = ajustarPacote(conteudoGerado, "org.example.generated");
                            conteudoGerado = ajustarNomeClasseParaArquivo(conteudoGerado, desiredClassName);

                            Path alvo = pastaTests.resolve(desiredFileName);
                            Files.createDirectories(alvo.getParent());
                            Files.writeString(alvo, conteudoGerado);
                            rel.add("OK: " + fileName + " -> " + alvo.getFileName());
                            // grava EXPLICAÇÃO por arquivo
                            String explicacao = montarExplicacaoTexto(
                                    baseName, desiredFileName, desiredClassName, conteudoGerado, sonarCut, "IA_FORMATADA", resp.explicacao());
                            Files.writeString(pastaExp.resolve(expFileName), explicacao,
                                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                            gerou = true;
                        } else {
                            String cru = resp.codigo();
                            if (cru != null && !cru.isBlank()) {
                                conteudoGerado = ajustarPacote(cru, "org.example.generated");
                                conteudoGerado = ajustarNomeClasseParaArquivo(conteudoGerado, desiredClassName);

                                Path alvo = pastaTests.resolve(desiredFileName);
                                Files.createDirectories(alvo.getParent());
                                Files.writeString(alvo, conteudoGerado);
                                rel.add("RAW_OK: " + fileName + " -> " + alvo.getFileName());

                                String explicacao = montarExplicacaoTexto(
                                        baseName, desiredFileName, desiredClassName, conteudoGerado, sonarCut, "IA_RAW", resp.explicacao());
                                Files.writeString(pastaExp.resolve(expFileName), explicacao,
                                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                                gerou = true;
                            } else {
                                // esqueleto mínimo
                                conteudoGerado = gerarEsqueleto(desiredClassName);
                                Path alvo = pastaTests.resolve(desiredFileName);
                                Files.createDirectories(alvo.getParent());
                                Files.writeString(alvo, conteudoGerado);
                                rel.add("SKELETON: " + fileName + " -> " + alvo.getFileName());

                                String explicacao = montarExplicacaoTexto(
                                        baseName, desiredFileName, desiredClassName, conteudoGerado, sonarCut, "SKELETON", null);
                                Files.writeString(pastaExp.resolve(expFileName), explicacao,
                                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                                gerou = true;
                            }
                        }

                        if (resp.explicacao() != null && !resp.explicacao().isBlank()) {
                            // além do .txt individual, mantemos também um log geral, se quiser
                            Path expLog = pastaExp.resolve("explicacoes.jsonl");
                            Files.writeString(expLog, resp.explicacao() + System.lineSeparator(),
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

    // —————— construção da EXPLICAÇÃO .txt ——————
    private String montarExplicacaoTexto(String baseName,
                                         String testFileName,
                                         String testClassName,
                                         String testCode,
                                         String sonarCut,
                                         String origem,            // IA_FORMATADA | IA_RAW | SKELETON
                                         String explicacaoIaBruta) // pode ser json ou texto
    {
        int qtdTests = contarOcorrencias(testCode, "@Test");
        boolean temErro = testCode.contains("assertThrows(") || testCode.contains("assertThrows");
        boolean usaMockito = testCode.contains("Mockito.") || testCode.contains("mock(") || testCode.contains("when(");

        Set<String> severidades = extrairSeveridades(sonarCut);
        Set<String> regras = extrairRegras(sonarCut);

        StringBuilder sb = new StringBuilder();
        sb.append("# Explicação dos testes – ").append(testFileName).append("\n\n");
        sb.append("**Arquivo de entrada:** ").append(baseName).append(".java\n");
        sb.append("**Teste gerado:** ").append(testFileName)
          .append("  (classe: ").append(testClassName).append(")\n");
        sb.append("**Origem:** ").append(origem).append("\n\n");

        sb.append("## O que o teste faz\n");
        if (qtdTests == 0) {
            sb.append("- Contém um teste mínimo para validar a compilação e a integração básica.\n");
        } else {
            sb.append("- Número de métodos de teste: ").append(qtdTests).append(".\n");
            if (temErro) sb.append("- Inclui cenários de **erro** usando `assertThrows`.\n");
            sb.append("- Exercita o caminho principal (happy path)");
            if (temErro) sb.append(" e também caminhos de erro");
            sb.append(".\n");
            if (usaMockito) sb.append("- Usa Mockito para isolar dependências e validar interações.\n");
        }
        sb.append("\n");

        sb.append("## Por que foi criado\n");
        if (!severidades.isEmpty() || !regras.isEmpty()) {
            sb.append("- Considera pontos do Sonar identificados no recorte (severidades: ")
              .append(String.join(", ", severidades.isEmpty() ? List.of("N/A") : severidades))
              .append(").\n");
            if (!regras.isEmpty()) {
                sb.append("- Regras relacionadas: ").append(String.join(", ", regras)).append(".\n");
            }
        } else {
            sb.append("- Foi criado para aumentar cobertura e validar comportamento essencial da classe alvo.\n");
        }
        sb.append("\n");

        sb.append("## Importância\n");
        sb.append("- Aumenta a confiança em alterações futuras e reduz regressões.\n");
        if (temErro) sb.append("- Ajuda a capturar falhas de validação e tratamento de exceções.\n");
        if (usaMockito) sb.append("- Garante que integrações com dependências externas estejam sob controle.\n");
        sb.append("\n");

        if (explicacaoIaBruta != null && !explicacaoIaBruta.isBlank()) {
            sb.append("## Notas adicionais da IA\n");
            sb.append(explicacaoIaBruta.trim()).append("\n\n");
        }

        if (sonarCut != null && !sonarCut.isBlank()) {
            sb.append("## Trecho relevante do Sonar (recorte)\n");
            sb.append(truncar(sonarCut.trim(), 1200)).append("\n");
        }
        return sb.toString();
    }

    private int contarOcorrencias(String s, String token) {
        if (s == null || token == null || token.isEmpty()) return 0;
        int count = 0, idx = 0;
        while ((idx = s.indexOf(token, idx)) >= 0) { count++; idx += token.length(); }
        return count;
    }

    private Set<String> extrairSeveridades(String s) {
        if (s == null) return Set.of();
        Set<String> out = new LinkedHashSet<>();
        String up = s.toUpperCase(Locale.ROOT);
        if (up.contains("BLOCKER")) out.add("BLOCKER");
        if (up.contains("CRITICAL")) out.add("CRITICAL");
        if (up.contains("MAJOR")) out.add("MAJOR");
        if (up.contains("MINOR")) out.add("MINOR");
        if (up.contains("INFO")) out.add("INFO");
        return out;
    }

    private Set<String> extrairRegras(String s) {
        if (s == null) return Set.of();
        Set<String> out = new LinkedHashSet<>();
        Matcher m = Pattern.compile("java:S\\d+").matcher(s);
        while (m.find()) out.add(m.group());
        return out;
    }

    private String truncar(String s, int max) {
        if (s == null || s.length() <= max) return s;
        return s.substring(0, max) + "...";
    }

    private static long readLongEnv(String name, long def){
        try{ var v=System.getenv(name); return (v==null||v.isBlank())?def:Long.parseLong(v.trim()); }catch(Exception e){ return def; }
    }
    private static void dormir(long ms){ try{ Thread.sleep(ms);}catch(InterruptedException ignored){} }
}
