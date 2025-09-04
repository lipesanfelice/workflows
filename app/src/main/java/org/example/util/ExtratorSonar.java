package org.example.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.net.URL;
import java.util.Base64;

public class ExtratorSonar {

    public static String extrair(String projeto, String token) {
        try {
            String url = "https://sonarcloud.io/api/measures/component?component=" + projeto +
                    "&metricKeys=coverage,lines_to_cover,uncovered_lines,complexity,bugs,vulnerabilities,code_smells,duplicated_lines_density";
            URL obj = new URL(url);
            HttpURLConnection conexao = (HttpURLConnection) obj.openConnection();
            conexao.setRequestMethod("GET");
            String auth = token + ":";
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            conexao.setRequestProperty("Authorization", "Basic " + encodedAuth);

            int status = conexao.getResponseCode();
            var in = new BufferedReader(new InputStreamReader(
                    status >= 200 && status < 300 ? conexao.getInputStream() : conexao.getErrorStream(),
                    StandardCharsets.UTF_8));
            String entrada;
            StringBuilder resposta = new StringBuilder();
            while ((entrada = in.readLine()) != null) {
                resposta.append(entrada);
            }
            in.close();
            return resposta.toString();
        } catch (Exception e) {
            return "{}";
        }
    }

    public static Path extrairESalvar(String projeto, String token, Path arquivoSaida) {
        try {
            String json = extrair(projeto, token);
            if (arquivoSaida.getParent() != null) Files.createDirectories(arquivoSaida.getParent());
            Files.writeString(arquivoSaida, json, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return arquivoSaida;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
