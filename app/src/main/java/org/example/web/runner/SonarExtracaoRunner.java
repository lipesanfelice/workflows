package org.example.web.runner;

import org.example.util.ExtratorSonar;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

@Component
public class SonarExtracaoRunner implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        if (!args.containsOption("acao")) return;
        var acao = args.getOptionValues("acao").get(0);
        if (!"extrair-sonar".equalsIgnoreCase(acao)) return;

        var ok = true;
        try {
            var projeto = System.getenv().getOrDefault("SONAR_PROJECT_KEY", "lipesanfelice_workflows");
            var token = System.getenv("SONAR_TOKEN");
            var outDir = System.getenv("SONAR_OUTPUT_DIR");
            var base = (outDir != null && !outDir.isBlank()) ? Paths.get(outDir) : Paths.get("entrada-usuario");
            var destino = base.resolve("relatorio-sonar.json");
            if (destino.getParent() != null) Files.createDirectories(destino.getParent());
            if (token == null || token.isBlank()) throw new IllegalStateException("SONAR_TOKEN ausente");
            ExtratorSonar.extrairESalvar(projeto, token, destino);
            System.out.println("user.dir=" + System.getProperty("user.dir"));
            System.out.println("Arquivo salvo em: " + destino.toAbsolutePath());
            System.out.println("Existe=" + Files.exists(destino));
        } catch (Exception e) {
            ok = false;
            System.err.println("Falha ao extrair Sonar: " + e.getMessage());
        } finally {
            var falhar = Boolean.parseBoolean(System.getenv().getOrDefault("FAIL_ON_SONAR_EXTRACTION_ERROR","false"));
            System.exit(ok ? 0 : (falhar ? 1 : 0));
        }
    }
}
