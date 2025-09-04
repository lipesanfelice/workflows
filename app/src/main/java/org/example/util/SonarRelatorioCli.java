package org.example.util;

import java.nio.file.Path;
import java.nio.file.Paths;

public class SonarRelatorioCli {
    public static void main(String[] args) {
        var projeto = "lipesanfelice_workflows";
        var token = System.getenv().getOrDefault("SONAR_TOKEN", args.length > 1 ? args[1] : "");
        var destino = args.length > 2 ? args[2] : "entrada-usuario/relatorio-sonar.json";
        if (projeto.isBlank() || token.isBlank()) {
            System.err.println("Defina SONAR_PROJECT e SONAR_TOKEN");
            System.exit(1);
        }
        var caminho = Paths.get(destino);
        ExtratorSonar.salvar(projeto, token, caminho);
        System.out.println(caminho.toAbsolutePath());
    }
}
