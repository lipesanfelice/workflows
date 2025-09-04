package org.example.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Base64;

public class ExtratorSonar {
    public static String extrair(String projeto, String token) {
        try {
            var base = "https://sonarcloud.io/api/measures/component?component=%s&metricKeys=coverage,lines_to_cover,uncovered_lines,complexity,bugs,vulnerabilities,code_smells,duplicated_lines_density";
            var url = String.format(base, URLEncoder.encode(projeto, StandardCharsets.UTF_8));
            for (int i = 0; i < 10; i++) {
                var conexao = (HttpURLConnection) new URL(url).openConnection();
                conexao.setRequestMethod("GET");
                var auth = Base64.getEncoder().encodeToString((token + ":").getBytes(StandardCharsets.UTF_8));
                conexao.setRequestProperty("Authorization", "Basic " + auth);
                var codigo = conexao.getResponseCode();
                if (codigo == 200) {
                    try (var in = new BufferedReader(new InputStreamReader(conexao.getInputStream(), StandardCharsets.UTF_8))) {
                        var sb = new StringBuilder();
                        String linha;
                        while ((linha = in.readLine()) != null) sb.append(linha);
                        return sb.toString();
                    }
                }
                Thread.sleep(3000);
            }
            return "{}";
        } catch (Exception e) {
            return "{}";
        }
    }

    public static Path salvar(String projeto, String token, Path destino) {
        try {
            Files.createDirectories(destino.getParent());
            var json = extrair(projeto, token);
            Files.writeString(destino, json, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return destino;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
