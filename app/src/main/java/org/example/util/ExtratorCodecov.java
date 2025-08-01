package org.example.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

public class ExtratorCodecov {

    public static void extrair(String owner, String repo, String token) {
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

            System.out.println("🔍 Resposta bruta da API:");
            System.out.println(resposta.toString());

            JSONObject json = new JSONObject(resposta.toString());


            //JSONObject commit = json.getJSONObject("commit");
            // Pega o primeiro commit da lista "results"
            JSONObject commit = json.getJSONArray("results").getJSONObject(0);

            // Extrai a cobertura
            JSONObject cobertura = commit.getJSONObject("totals");

            double coverage = cobertura.getDouble("coverage");
            int linhasCobertas = cobertura.getInt("hits");
            int linhasTotais = cobertura.getInt("lines");

            // Informações adicionais
            String hash = commit.getString("commitid");
            String data = commit.getString("timestamp");

            System.out.println("📊 MÉTRICAS EXTRAÍDAS DO CODECOV:");
            System.out.println("➡ Cobertura total: " + String.format("%.2f", coverage) + "%");
            System.out.println("➡ Linhas cobertas: " + linhasCobertas + " / " + linhasTotais);
            System.out.println("➡ Último commit: " + hash);
            System.out.println("➡ Data: " + data);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Erro ao extrair dados do Codecov");
        }
    }
}
