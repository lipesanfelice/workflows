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
                String caminho = arq.getKey().trim().replace('.', '/');
                String nome = caminho.endsWith(".java") ? caminho : caminho + ".java";
                Path alvo = pastaTests.resolve(nome);
                Files.createDirectories(alvo.getParent());
                Files.writeString(alvo, arq.getValue());
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
}
