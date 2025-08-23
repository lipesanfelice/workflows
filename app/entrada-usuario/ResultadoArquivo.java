package org.example.web.dto;

public class ResultadoArquivo {
    private String caminho;
    private long tamanhoBytes;
    private boolean isJava;
    private String mensagem;

    public ResultadoArquivo() {}

    public ResultadoArquivo(String caminho, long tamanhoBytes, boolean isJava, String mensagem) {
        this.caminho = caminho;
        this.tamanhoBytes = tamanhoBytes;
        this.isJava = isJava;
        this.mensagem = mensagem;
    }

    public String getCaminho() { return caminho; }
    public long getTamanhoBytes() { return tamanhoBytes; }
    public boolean isJava() { return isJava; }
    public String getMensagem() { return mensagem; }

    public void setCaminho(String caminho) { this.caminho = caminho; }
    public void setTamanhoBytes(long tamanhoBytes) { this.tamanhoBytes = tamanhoBytes; }
    public void setJava(boolean aJava) { isJava = aJava; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
}
