package org.example.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class ExtratorSonar {

    public static String extrair(String projeto, String token) {
        try {
            // URL base da API do SonarCloud
            String url = "https://sonarcloud.io/api/measures/component?component=" + projeto +
                         "&metricKeys=coverage,lines_to_cover,uncovered_lines,complexity,bugs,vulnerabilities,code_smells,duplicated_lines_density";

            URL obj = new URL(url);
            HttpURLConnection conexao = (HttpURLConnection) obj.openConnection();
            conexao.setRequestMethod("GET");

            // Autenticação básica com token
            String auth = token + ":";
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            conexao.setRequestProperty("Authorization", "Basic " + encodedAuth);

            // Leitura da resposta
            BufferedReader in = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
            String entrada;
            StringBuilder resposta = new StringBuilder();

            while ((entrada = in.readLine()) != null) {
                resposta.append(entrada);
            }
            in.close();

            System.out.println("Resposta da API SonarCloud:");
            System.out.println(resposta);

            return resposta.toString();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erro ao extrair dados do SonarCloud");
            return "{}"; // Retorna JSON vazio em caso de erro
        }
    }
}
