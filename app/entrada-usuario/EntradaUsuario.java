package org.example.erros;

import java.util.*;  // import desnecessário

public class ProgramaBugado {

    private int contador;
    private String nome;

    public ProgramaBugado(String nome) {
        this.nome = nome;
        this.contador = 0;
    }

    public void incrementar() {
        contador = contador + 1;
    }

    // Erro: retorna void mas tem return int
    public void getContador() {
        return contador;
    }

    // Erro: variável nunca usada
    private double numeroOculto = 42.0;

    // Erro de NullPointer possível
    public int tamanhoNome() {
        return nome.length(); // se nome == null → crash
    }

    // Erro lógico: divisão por zero se lista vazia
    public double media(List<Integer> numeros) {
        int soma = 0;
        for (int n : numeros) {
            soma += n;
        }
        return soma / numeros.size(); // divisão inteira + possível zero
    }

    public static void main(String[] args) {
        ProgramaBugado prog = new ProgramaBugado(null); // nome null

        // Erro: chamada de método inexistente
        prog.naoExiste();

        // Erro: loop infinito
        while (true) {
            System.out.println("Rodando...");
        }

        // Código morto (nunca executado)
        System.out.println("Fim do programa");
    }
}