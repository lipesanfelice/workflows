package org.example.model;

import java.util.List;

public class ClasseCoberta {
    private String nomeClasse;
    private String caminhoArquivo;
    private List<MetodoCoberto> metodos;

    public ClasseCoberta(String nomeClasse, String caminhoArquivo, List<MetodoCoberto> metodos) {
        this.nomeClasse = nomeClasse;
        this.caminhoArquivo = caminhoArquivo;
        this.metodos = metodos;
    }

    public String getNomeClasse() {
        return nomeClasse;
    }

    public String getCaminhoArquivo() {
        return caminhoArquivo;
    }

    public List<MetodoCoberto> getMetodos() {
        return metodos;
    }
}
