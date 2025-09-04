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
            var base = Paths.get(dirEntrada).resolve("testes_explicações");
            var pastaTests = base.resolve("tests");
            var pastaExp = base.resolve("explicacoes");
            Files.createDirectories(pastaTests);
            Files.createDirectories(pastaExp);

            var resp = ia.gerar(prompt);
            var arquivos = separarArquivos(resp.codigo());
            var nomes = new ArrayList<String>();
            for (var arq : arquivos.entrySet()) {
                var caminho = arq.getKey().trim().replace('.', '/');
                var nome = caminho.endsWith(".java") ? caminho : caminho + ".java";
                var alvo = pastaTests.resolve(nome);
                Files.createDirectories(alvo.getParent());
                Files.writeString(alvo, arq.getValue());
                nomes.add(alvo.toString());
            }
            if (!resp.explicacao().isBlank()) {
                var exp = pastaExp.resolve("explicacoes.jsonl");
                Files.writeString(exp, resp.explicacao() + System.lineSeparator(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
            var resumo = base.resolve("relatorio.txt");
            var linhas = String.join(System.lineSeparator(), nomes);
            Files.writeString(resumo, linhas + System.lineSeparator(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return base;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String,String> separarArquivos(String bloco) {
        var mapa = new LinkedHashMap<String,String>();
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
