package org.example;

import org.example.model.RelatorioCobertura;
import org.example.Parser.*;
import org.example.util.*;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("=== SISTEMA DE ANÁLISE DE COBERTURA ===");
            
            // 1. Jacoco - Arquivo local
            testarJacoco();
            
            // 2. SonarCloud - API online
            testarSonarCloud();
            
            // 3. Codecov - API online
            testarCodecov();
            
        } catch (Exception e) {
            System.err.println("Erro no processamento: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testarJacoco() {
        System.out.println("\n[1] TESTANDO JACOCO (ARQUIVO LOCAL)");
        try {
            // Extração
            String caminhoRelatorio = "app/build/reports/jacoco/test/jacocoTestReport.xml";
            ExtratorJacoco.extrair(caminhoRelatorio); // Mostra dados brutos no console
            
            // Parsing
            RelatorioCobertura relatorio = ParserJacoco.parse(new File(caminhoRelatorio));
            imprimirRelatorio(relatorio);
            
        } catch (Exception e) {
            System.err.println("Erro no processamento Jacoco: " + e.getMessage());
        }
    }

    private static void testarSonarCloud() {
        System.out.println("\n[2] TESTANDO SONARCLOUD (API)");
        try {
            // Configurações (substitua com seus dados reais)
            String projeto = "lipesanfelice_workflows";
            String token = "869a0095d5eeff251ea1d9ea8bdd34cc9e4cbe38";
            
            // Extração
            String jsonSonar = ExtratorSonar.extrair(projeto, token); // Retorna o JSON completo
            
            // Parsing
            RelatorioCobertura relatorio = ParserSonar.parse(jsonSonar);
            imprimirRelatorio(relatorio);
            
        } catch (Exception e) {
            System.err.println("Erro no processamento SonarCloud: " + e.getMessage());
        }
    }

    private static void testarCodecov() {
        System.out.println("\n[3] TESTANDO CODECOV (API)");
        try {
            // Configurações (substitua com seus dados reais)
            String owner = "lipesanfelice";
            String repo = "workflows";
            String token = "54705329-755b-42f7-ba92-d00741fb093c";
            
            // Extração
            String jsonCodecov = ExtratorCodecov.extrair(owner, repo, token);
            
            // Parsing
            RelatorioCobertura relatorio = ParserCodecov.parse(jsonCodecov);
            imprimirRelatorio(relatorio);
            
        } catch (Exception e) {
            System.err.println("Erro no processamento Codecov: " + e.getMessage());
        }
    }

    private static void imprimirRelatorio(RelatorioCobertura relatorio) {
        if (relatorio == null) {
            System.err.println("Relatório não gerado (null)");
            return;
        }
        
        System.out.println("\n=== RELATÓRIO ESTRUTURADO ===");
        System.out.println("Projeto: " + relatorio.getNomeProjeto());
        System.out.println("Ferramenta: " + relatorio.getFerramenta());
        System.out.println("\nClasses analisadas:");
        
        relatorio.getClasses().forEach(classe -> {
            System.out.println("\n  Classe: " + classe.getNomeClasse());
            System.out.println("  Arquivo: " + classe.getCaminhoArquivo());
            
            classe.getMetodos().forEach(metodo -> {
                System.out.printf("  - %s (Linhas %d-%d): %.2f%% cobertura%n",
                    metodo.getNomeMetodo(),
                    metodo.getLinhaInicio(),
                    metodo.getLinhaFim(),
                    metodo.getCobertura());
            });
        });
    }
}