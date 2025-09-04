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

@Service
public class GeradorTestesService {
    private final ClienteIa ia;

    @Value("${app.entrada.diretorio:entrada-usuario}")
    private String dirEntrada;

    // limites bem conservadores para o tier grátis
    private static final int MAX_CODE_CHARS = 8000;
    private static final int MAX_SONAR_CHARS = 6000;

    public GeradorTestesService(ClienteIa ia) {
        this.ia = ia;
    }

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
            List<String> linhasRelatorio = new ArrayList<>();

            for (Path arq : arquivos) {
                try {
                    String codigo = LeitorCodigo.lerAteLimite(arq, MAX_CODE_CHARS);
                    String sonarCut = SonarUtil.extrairTrechoPorArquivo(sonarJson, arq.getFileName().toString(), MAX_SONAR_CHARS);
                    String prompt = Prompts.montarPromptGroqPorArquivo(sonarCut, arq.toString(), codigo);

                    var resp = ia.gerar(prompt);

                    Map<String,String> arquivosGerados = separarArquivos(resp.codigo());
                    if (arquivosGerados.isEmpty()) {
                        linhasRelatorio.add("SEM_ARQUIVOS: " + arq.getFileName());
                    } else {
                        for (var e : arquivosGerados.entrySet()) {
                            String nomeNorm = normalizarCaminho(e.getKey());
                            Path alvo = pastaTests.resolve(nomeNorm);
                            Files.createDirectories(alvo.getParent());
                            String conteudo = ajustarPacote(e.getValue(), "org.example.generated");
                            Files.writeString(alvo, conteudo);
                            linhasRelatorio.add("OK: " + arq.getFileName() + " -> " + alvo);
                        }
                    }
                    if (resp.explicacao() != null && !resp.explicacao().isBlank()) {
                        Path exp = pastaExp.resolve("explicacoes.jsonl");
                        Files.writeString(exp, resp.explicacao() + System.lineSeparator(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    }

                    // pequeno intervalo para não pressionar TPM
                    try { Thread.sleep(350L); } catch (InterruptedException ignored) {}
                } catch (Exception ex) {
                    linhasRelatorio.add("ERRO: " + arq.getFileName() + " -> " + ex.getMessage());
                    // continua nos próximos arquivos
                }
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
        try (var walk = Files.walk(baseEntrada, 1)) { // nivel 1 porque você achata os arquivos
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
}
