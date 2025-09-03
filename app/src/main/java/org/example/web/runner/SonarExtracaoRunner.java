package org.example.web.runner;

import org.example.util.ExtratorSonar;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class SonarExtracaoRunner implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        if (!args.containsOption("acao")) return;
        var acao = args.getOptionValues("acao").get(0);
        if (!"extrair-sonar".equalsIgnoreCase(acao)) return;

        var ok = true;
        try {
            var projeto = "lipesanfelice_workflows";
            var token = System.getenv("SONAR_TOKEN");
            var destino = Paths.get("entrada-usuario").resolve("relatorio-sonar.json");
            if (token == null || token.isBlank()) throw new IllegalStateException("SONAR_TOKEN ausente");
            ExtratorSonar.extrairESalvar(projeto, token, destino);
            System.out.println("relatorio-sonar.json salvo em: " + destino.toAbsolutePath());
        } catch (Exception e) {
            ok = false;
            System.err.println("Falha ao extrair Sonar: " + e.getMessage());
        } finally {
            var failOnError = Boolean.parseBoolean(System.getenv().getOrDefault("FAIL_ON_SONAR_EXTRACTION_ERROR","false"));
            var code = ok ? 0 : (failOnError ? 1 : 0);
            System.out.flush();
            System.err.flush();
            System.exit(code);
        }
    }
}
