package org.example.model;

import java.util.List;

public class RelatorioCobertura {
    private String nomeProjeto;
    private List<ClasseCoberta> classes;
    private Ferramenta ferramenta;

    public RelatorioCobertura(String nomeProjeto, List<ClasseCoberta> classes, Ferramenta ferramenta) {
        this.nomeProjeto = nomeProjeto;
        this.classes = classes;
        this.ferramenta = ferramenta;
    }

    public String getNomeProjeto() {
        return nomeProjeto;
    }

    public List<ClasseCoberta> getClasses() {
        return classes;
    }

    public Ferramenta getFerramenta() {
        return ferramenta;
    }

    public enum Ferramenta {
        JACOCO, SONARQUBE, CODECOV
    }
}
