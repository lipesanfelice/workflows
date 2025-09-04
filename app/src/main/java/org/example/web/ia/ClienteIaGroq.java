package org.example.web.ia;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClienteIaGroq implements ClienteIa {
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
    private final ObjectMapper json = new ObjectMapper();
    private final URI uri = URI.create("https://api.groq.com/openai/v1/chat/completions");
    private final String chave;
    private final String modelo;

    public ClienteIaGroq(String chave, String modelo) {
        this.chave = chave;
        this.modelo = modelo;
    }

    @Override
    public Registro gerar(String prompt) {
        try {
            var corpo = """
            {"model":"%s","messages":[{"role":"user","content":%s}],"temperature":0.1}
            """.formatted(modelo, json.writeValueAsString(prompt));
            var req = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Authorization","Bearer " + chave)
                    .header("Content-Type","application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(corpo))
                    .build();
            var resp = http.send(req, HttpResponse.BodyHandlers.ofString()).body();
            var raiz = json.readTree(resp);
            var conteudo = raiz.path("choices").get(0).path("message").path("content").asText("");
            var codigo = extrair(conteudo, "====CODIGO====", "====FIM-CODIGO====");
            var explicacao = extrair(conteudo, "====EXPLICACAO====", "====FIM-EXPLICACAO====");
            return new Registro(codigo, explicacao);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String extrair(String s, String ini, String fim) {
        var a = s.indexOf(ini);
        var b = s.indexOf(fim);
        if (a < 0 || b < 0 || b <= a) return "";
        return s.substring(a + ini.length(), b).trim();
    }
}
