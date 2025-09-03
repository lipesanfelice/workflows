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
    public void run(ApplicationArguments args) throws Exception {
        if (!args.containsOption("acao")) return;
        var acao = args.getOptionValues("acao").get(0);
        if (!"extrair-sonar".equalsIgnoreCase(acao)) return;

        var projeto = "lipesanfelice_workflows";
        var token = System.getenv("SONAR_TOKEN");
        var destino = Paths.get("entrada-usuario").resolve("relatorio-sonar.json");

        if (projeto == null || token == null) throw new IllegalStateException("Variaveis SONAR_PROJECT_KEY e SONAR_TOKEN obrigatorias");
        ExtratorSonar.extrairESalvar(projeto, token, destino);
        System.out.println("relatorio-sonar.json salvo em: " + destino.toAbsolutePath());
    }
}
