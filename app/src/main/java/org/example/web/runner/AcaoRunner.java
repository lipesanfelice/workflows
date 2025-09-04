package org.example.web.runner;

import org.example.web.service.GeradorTestesService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.boot.SpringApplication;

@Component
public class AcaoRunner implements ApplicationRunner {
    private final GeradorTestesService gerador;
    private final ApplicationContext ctx;

    public AcaoRunner(GeradorTestesService gerador, ApplicationContext ctx) {
        this.gerador = gerador;
        this.ctx = ctx;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!args.containsOption("acao")) return;
        var v = args.getOptionValues("acao").get(0);
        if (!"gerar-testes".equalsIgnoreCase(v)) return;

        var relatorio = System.getenv("SONAR_RELATORIO_JSON");
        gerador.gerarParaTodosArquivos(relatorio != null ? relatorio : "");

        SpringApplication.exit(ctx, () -> 0);
    }
}
