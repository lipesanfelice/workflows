package org.example.web.runner;

import org.example.web.service.GeradorTestesService;
import org.example.util.Prompts;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class AcaoRunner implements ApplicationRunner {
    private final GeradorTestesService gerador;

    public AcaoRunner(GeradorTestesService gerador) {
        this.gerador = gerador;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!args.containsOption("acao")) return;
        var valor = args.getOptionValues("acao").get(0);
        if (!"gerar-testes".equalsIgnoreCase(valor)) return;

        var relatorio = System.getenv("SONAR_RELATORIO_JSON");
        var codigoDir = System.getenv("CODIGO_FONTE_DIR");
        var findings = Files.readString(Path.of(relatorio));
        var prompt = Prompts.montarPromptGroq(findings, Path.of(codigoDir).toAbsolutePath().toString());
        gerador.gerar(prompt);
    }
}
