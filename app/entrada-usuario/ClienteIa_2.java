package org.example.web.ia;

public interface ClienteIa {
    Registro gerar(String prompt);
    record Registro(String codigo, String explicacao) {}
}
