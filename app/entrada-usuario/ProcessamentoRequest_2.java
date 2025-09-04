package org.example.web.dto;

import jakarta.validation.constraints.NotBlank;

public class ProcessamentoRequest {
    @NotBlank
    private String codigo;

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
}
