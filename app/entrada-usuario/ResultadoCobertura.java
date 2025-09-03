package org.example.model;

import java.util.List;
import java.util.Map;

public class ResultadoCobertura {
    private String ferramenta; // Ex: JaCoCo, Codecov
    private String nomeArquivo; // Ex: MinhaClasse.java
    private Map<String, List<InformacaoMetodo>> metodosPorClasse;

    public ResultadoCobertura(String ferramenta, String nomeArquivo, Map<String, List<InformacaoMetodo>> metodosPorClasse) {
        this.ferramenta = ferramenta;
        this.nomeArquivo = nomeArquivo;
        this.metodosPorClasse = metodosPorClasse;
    }

    public String getFerramenta() {
        return ferramenta;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public Map<String, List<InformacaoMetodo>> getMetodosPorClasse() {
        return metodosPorClasse;
    }

    public static class InformacaoMetodo {
        private String nomeMetodo;
        private int linhaInicio;
        private int linhaFim;
        private int cobertura; // porcentagem de cobertura

        public InformacaoMetodo(String nomeMetodo, int linhaInicio, int linhaFim, int cobertura) {
            this.nomeMetodo = nomeMetodo;
            this.linhaInicio = linhaInicio;
            this.linhaFim = linhaFim;
            this.cobertura = cobertura;
        }

        public String getNomeMetodo() {
            return nomeMetodo;
        }

        public int getLinhaInicio() {
            return linhaInicio;
        }

        public int getLinhaFim() {
            return linhaFim;
        }

        public int getCobertura() {
            return cobertura;
        }
    }
}
