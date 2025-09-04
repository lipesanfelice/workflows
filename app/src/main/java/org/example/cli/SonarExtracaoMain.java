package org.example.cli;

import org.example.util.ExtratorSonar;
import java.nio.file.*;

public class SonarExtracaoMain {
    public static void main(String[] args) {
        try {
            var projeto = System.getenv().getOrDefault("SONAR_PROJECT_KEY", "lipesanfelice_workflows");
            var token = System.getenv("SONAR_TOKEN");
            var saidaEnv = System.getenv("SONAR_OUTPUT_FILE");
            var destino = saidaEnv != null && !saidaEnv.isBlank()
                    ? Paths.get(saidaEnv).toAbsolutePath()
                    : Paths.get("").toAbsolutePath().resolve("app").resolve("dados_sonar").resolve("relatorio-sonar.json");
            if (destino.getParent() != null) Files.createDirectories(destino.getParent());
            if (token == null || token.isBlank()) {
                System.err.println("SONAR_TOKEN ausente");
                System.exit(0);
            }
            ExtratorSonar.extrairESalvar(projeto, token, destino);
            var existe = Files.exists(destino);
            var tam = existe ? Files.size(destino) : -1L;
            System.out.println("arquivo=" + destino + " existe=" + existe + " tamanho=" + tam);
            System.exit(0);
        } catch (Exception e) {
            System.err.println("Falha: " + e.getMessage());
            System.exit(0);
        }
    }
}
