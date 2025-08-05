package org.example.util;

public class DadoCobertura {
    public String nomeClasse;
    public String nomeMetodo;
    public int linhaInicial;
    public int linhaFinal;
    public float cobertura;

    public DadoCobertura(String nomeClasse, String nomeMetodo, int linhaInicial, int linhaFinal, float cobertura) {
        this.nomeClasse = nomeClasse;
        this.nomeMetodo = nomeMetodo;
        this.linhaInicial = linhaInicial;
        this.linhaFinal = linhaFinal;
        this.cobertura = cobertura;
    }

    @Override
    public String toString() {
        return nomeClasse + "#" + nomeMetodo + " [" + linhaInicial + "-" + linhaFinal + "] -> " + cobertura + "%";
    }
}
