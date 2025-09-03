package org.example.web.runner;

import org.example.util.ExtratorSonar;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.file.*;
import java.nio.charset.StandardCharsets;

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
            var outDirEnv = System.getenv("SONAR_OUTPUT_DIR");
            Path base;
            if (outDirEnv != null && !outDirEnv.isBlank()) {
                base = Paths.get(outDirEnv);
            } else {
                var cwd = Paths.get("").toAbsolutePath();
                if (Files.isDirectory(cwd.resolve("app"))) {
                    base = cwd.resolve("app").resolve("entrada-usuario");
                } else {
                    base = cwd.resolve("entrada-usuario");
                }
            }
            var destino = base.resolve("relatorio-sonar.json");
            if (destino.getParent() != null) Files.createDirectories(destino.getParent());
            if (token == null || token.isBlank()) throw new IllegalStateException("SONAR_TOKEN ausente");
            ExtratorSonar.extrairESalvar(projeto, token, destino);
            var existe = Files.exists(destino);
            var tam = existe ? Files.size(destino) : -1L;
            System.out.println("user.dir=" + System.getProperty("user.dir"));
            System.out.println("saida.dir=" + base.toAbsolutePath());
            System.out.println("arquivo=" + destino.toAbsolutePath());
            System.out.println("existe=" + existe + " tamanho=" + tam);
            if (!existe || tam <= 0) throw new IllegalStateException("Arquivo nao foi criado ou esta vazio");
        } catch (Exception e) {
            ok = false;
            System.err.println("Falha ao extrair Sonar: " + e.getMessage());
        } finally {
            var falhar = Boolean.parseBoolean(System.getenv().getOrDefault("FAIL_ON_SONAR_EXTRACTION_ERROR","false"));
            System.exit(ok ? 0 : (falhar ? 1 : 0));
        }
    }
}
