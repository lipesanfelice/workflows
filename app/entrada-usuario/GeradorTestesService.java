package org.example.web.service;

import org.example.web.ia.ClienteIa;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.*;
import java.util.*;
import java.io.*;

@Service
public class GeradorTestesService {
    private final ClienteIa ia;

    @Value("${app.entrada.diretorio:entrada-usuario}")
    private String dirEntrada;

    public GeradorTestesService(ClienteIa ia) {
        this.ia = ia;
    }

    public Path gerar(String prompt) {
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

            var resp = ia.gerar(prompt);
            var arquivos = separarArquivos(resp.codigo());
            var nomes = new ArrayList<String>();
            for (var arq : arquivos.entrySet()) {
                String nomeNormalizado = normalizarCaminho(arq.getKey());
                Path alvo = pastaTests.resolve(nomeNormalizado);
                Files.createDirectories(alvo.getParent());
                String conteudoAjustado = ajustarPacote(arq.getValue(), "org.example.generated");
                Files.writeString(alvo, conteudoAjustado);
                nomes.add(alvo.toString());
            }
            if (resp.explicacao() != null && !resp.explicacao().isBlank()) {
                Path exp = pastaExp.resolve("explicacoes.jsonl");
                Files.writeString(exp, resp.explicacao() + System.lineSeparator(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
            Path resumo = base.resolve("relatorio.txt");
            String linhas = String.join(System.lineSeparator(), nomes);
            Files.writeString(resumo, linhas + System.lineSeparator(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return base;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    // === Blindagens contra nomes "tortos" vindos da IA ===
    private String normalizarCaminho(String nome) {
        if (nome == null || nome.isBlank()) {
            return "org/example/generated/ArquivoGeradoTest.java";
        }
        String s = nome.trim().replace('\\','/');

        // Se veio só com pontos (org.example.generated.ClasseTest.java), vira path
        if (!s.contains("/")) {
            s = s.replace('.', '/');
        }

        // Remove barras duplas e prefixos estranhos
        s = s.replaceAll("/{2,}", "/");
        while (s.startsWith("/")) s = s.substring(1);

        // Caso bizarro: ".../EntradaUsuarioTest/java.java" -> ".../EntradaUsuarioTest.java"
        if (s.endsWith("/java.java")) {
            int idx = s.lastIndexOf('/', s.length() - "/java.java".length() - 1);
            if (idx >= 0) {
                String className = s.substring(idx + 1, s.length() - "/java.java".length());
                s = s.substring(0, idx + 1) + className + ".java";
            } else {
                s = s.substring(0, s.length() - "/java.java".length()) + ".java";
            }
        }

        // Garante extensão .java
        if (!s.endsWith(".java")) {
            s = s + ".java";
        }

        // Se não apontou pacote, cai no pacote de testes padrão
        if (!s.startsWith("org/")) {
            // mantém só o nome do arquivo
            String file = s.substring(s.lastIndexOf('/') + 1);
            s = "org/example/generated/" + file;
        }

        // Força pacote org/example/generated para ficar consistente com o conteúdo
        if (!s.startsWith("org/example/generated/")) {
            String file = s.substring(s.lastIndexOf('/') + 1);
            s = "org/example/generated/" + file;
        }

        return s;
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
