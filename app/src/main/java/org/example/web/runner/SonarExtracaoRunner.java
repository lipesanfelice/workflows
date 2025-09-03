package org.example.web.runner;

import org.example.util.ExtratorSonar;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.file.*;
import java.security.MessageDigest;

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
            var saida = System.getenv("SONAR_OUTPUT_FILE");
            if (token == null || token.isBlank()) throw new IllegalStateException("SONAR_TOKEN ausente");
            if (saida == null || saida.isBlank()) throw new IllegalStateException("SONAR_OUTPUT_FILE ausente");
            var destino = Paths.get(saida).toAbsolutePath();
            if (destino.getParent() != null) Files.createDirectories(destino.getParent());

            ExtratorSonar.extrairESalvar(projeto, token, destino);

            var existe = Files.exists(destino);
            var tam = existe ? Files.size(destino) : -1L;
            var sha = existe ? sha256Hex(Files.readAllBytes(destino)) : "NA";
            System.out.println("user.dir=" + System.getProperty("user.dir"));
            System.out.println("arquivo=" + destino);
            System.out.println("existe=" + existe + " tamanho=" + tam + " sha256=" + sha);

            if (!existe || tam <= 2) throw new IllegalStateException("Arquivo ausente ou vazio");
        } catch (Exception e) {
            ok = false;
            System.err.println("Falha ao extrair Sonar: " + e.getMessage());
        } finally {
            var falhar = Boolean.parseBoolean(System.getenv().getOrDefault("FAIL_ON_SONAR_EXTRACTION_ERROR","false"));
            System.exit(ok ? 0 : (falhar ? 1 : 0));
        }
    }

    private static String sha256Hex(byte[] dados) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            var h = md.digest(dados);
            var sb = new StringBuilder();
            for (byte b : h) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return "NA";
        }
    }
}
