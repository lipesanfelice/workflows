package org.example.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

public class ExtratorCodecov {

    public static List<DadoCobertura> extrair(String owner, String repo, String token) {
        List<DadoCobertura> dados = new ArrayList<>();
        try {
            String url = "https://api.codecov.io/api/v2/github/" + owner + "/repos/" + repo + "/commits?branch=main";

            URL obj = new URL(url);
            HttpURLConnection conexao = (HttpURLConnection) obj.openConnection();
            conexao.setRequestMethod("GET");
            conexao.setRequestProperty("Authorization", "Bearer " + token);

            BufferedReader in = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
            String entrada;
            StringBuilder resposta = new StringBuilder();

            while ((entrada = in.readLine()) != null) {
                resposta.append(entrada);
            }
            in.close();

            JSONObject json = new JSONObject(resposta.toString());
            JSONObject commit = json.getJSONArray("results").getJSONObject(0);
            JSONObject totals = commit.getJSONObject("totals");

            float coverage = (float) totals.getDouble("coverage");
            int linhasTotais = totals.getInt("lines");
            int linhasCobertas = totals.getInt("hits");

            dados.add(new DadoCobertura(
                repo,
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