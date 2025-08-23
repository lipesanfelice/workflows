package org.example.web.dto;

import java.util.ArrayList;
import java.util.List;

public class ResultadoProcessamento {
    private String idExecucao;
    private int totalArquivos;
    private int totalJava;
    private List<ResultadoArquivo> arquivos = new ArrayList<>();
    private String proximaAcao;

    public String getIdExecucao() { return idExecucao; }
    public void setIdExecucao(String idExecucao) { this.idExecucao = idExecucao; }
    public int getTotalArquivos() { return totalArquivos; }
    public void setTotalArquivos(int totalArquivos) { this.totalArquivos = totalArquivos; }
    public int getTotalJava() { return totalJava; }
    public void setTotalJava(int totalJava) { this.totalJava = totalJava; }
    public List<ResultadoArquivo> getArquivos() { return arquivos; }
    public void setArquivos(List<ResultadoArquivo> arquivos) { this.arquivos = arquivos; }
    public String getProximaAcao() { return proximaAcao; }
    public void setProximaAcao(String proximaAcao) { this.proximaAcao = proximaAcao; }
}
