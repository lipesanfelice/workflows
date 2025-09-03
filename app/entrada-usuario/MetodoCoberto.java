package org.example.model;

public class MetodoCoberto {
    private String nomeMetodo;
    private int linhaInicio;
    private int linhaFim;
    private double cobertura;

    public MetodoCoberto(String nomeMetodo, int linhaInicio, int linhaFim, double cobertura) {
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

    public double getCobertura() {
        return cobertura;
    }
}
