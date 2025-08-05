package org.example.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.json.JSONArray;

public class ExtratorSonar {

    public static List<DadoCobertura> extrair(String projeto, String token) {
        List<DadoCobertura> dados = new ArrayList<>();
        try {
            String url = "https://sonarcloud.io/api/measures/component?component=" + projeto +
                         "&metricKeys=coverage,lines_to_cover,uncovered_lines";

            URL obj = new URL(url);
            HttpURLConnection conexao = (HttpURLConnection) obj.openConnection();
            conexao.setRequestMethod("GET");

            String auth = token + ":";
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            conexao.setRequestProperty("Authorization", "Basic " + encodedAuth);

            BufferedReader in = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
            String entrada;
            StringBuilder resposta = new StringBuilder();

            while ((entrada = in.readLine()) != null) {
                resposta.append(entrada);
            }
            in.close();

            JSONObject json = new JSONObject(resposta.toString());
            JSONObject component = json.getJSONObject("component");
            JSONArray measures = component.getJSONArray("measures");

            float coverage = 0;
            int linhasTotais = 0;
            int linhasCobertas = 0;

            for (int i = 0; i < measures.length(); i++) {
                JSONObject measure = measures.getJSONObject(i);
                switch (measure.getString("metric")) {
                    case "coverage":
                        coverage = (float) measure.getDouble("value");
                        break;
                    case "lines_to_cover":
                        linhasTotais = measure.getInt("value");
                        break;
                    case "uncovered_lines":
                        linhasCobertas = linhasTotais - measure.getInt("value");
                        break;
                }
            }

            dados.add(new DadoCobertura(
                projeto,
                "Geral",
                1,
                linhasTotais,
                coverage
            ));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return dados;
    }
}