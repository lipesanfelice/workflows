package org.example.web.ia;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ClienteIaGroq implements ClienteIa {
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    private final ObjectMapper json = new ObjectMapper();
    private final URI uri = URI.create("https://api.groq.com/openai/v1/chat/completions");
    private final String chave;
    private final String modelo;

    private static final int MAX_RETRIES = 5;
    private static final Pattern WAIT_SECONDS = Pattern.compile("try again in\\s+([0-9]+(?:\\.[0-9]+)?)s", Pattern.CASE_INSENSITIVE);

    public ClienteIaGroq(String chave, String modelo) {
        this.chave = chave;
        this.modelo = modelo;
    }

    @Override
    public Registro gerar(String prompt) {
        String corpo = ("""
        {"model":"%s","messages":[{"role":"user","content":%s}],"temperature":0.1,"max_tokens":1000}
        """).formatted(modelo, jsonValue(prompt));
        int attempt = 0;

        while (true) {
            attempt++;
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(uri)
                        .timeout(Duration.ofSeconds(120))
                        .header("Authorization","Bearer " + chave)
                        .header("Content-Type","application/json")
                        .header("User-Agent","projeto-teste/1.0")
                        .POST(HttpRequest.BodyPublishers.ofString(corpo))
                        .build();
                HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

                int sc = resp.statusCode();
                if (sc >= 200 && sc < 300) {
                    var raiz = json.readTree(resp.body());
                    String conteudo = raiz.path("choices").get(0).path("message").path("content").asText("");
                    String codigo = extrair(conteudo, "====CODIGO====", "====FIM-CODIGO====");
                    String explicacao = extrair(conteudo, "====EXPLICACAO====", "====FIM-EXPLICACAO====");
                    return new Registro(codigo, explicacao);
                }

                // Tratamento 429 (TPM) com backoff baseado na dica do servidor
                if (sc == 429 && attempt < MAX_RETRIES) {
                    long aguardarMs = sugerirEsperaMs(resp.body());
                    dormir(aguardarMs);
                    continue;
                }

                // Tratamento 5xx com backoff exponencial leve
                if (sc >= 500 && sc < 600 && attempt < MAX_RETRIES) {
                    long backoff = (long) (Math.pow(2, attempt) * 500) + jitter(200, 600);
                    dormir(backoff);
                    continue;
                }

                throw new RuntimeException("Falha Groq: " + sc + " - " + resp.body());
            } catch (Exception e) {
                if (attempt < MAX_RETRIES && isRetriable(e)) {
                    dormir( 800L + jitter(200,600) );
                    continue;
                }
                throw new RuntimeException(e);
            }
        }
    }

    private static boolean isRetriable(Exception e) {
        String m = e.getMessage();
        if (m == null) return false;
        return m.contains("java.net") || m.contains("timed out");
    }

    private static long sugerirEsperaMs(String body) {
        try {
            Matcher m = WAIT_SECONDS.matcher(body == null ? "" : body);
            if (m.find()) {
                double s = Double.parseDouble(m.group(1));
                return (long) (s * 1000.0) + jitter(300, 900);
            }
        } catch (Exception ignored) {}
        // fallback conservador
        return 10_000L + jitter(300, 900);
    }

    private static long jitter(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private static void dormir(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    private String jsonValue(String s) {
        try { return json.writeValueAsString(s); } catch (Exception e) { throw new RuntimeException(e); }
    }

    private String extrair(String s, String ini, String fim) {
        if (s == null) return "";
        int a = s.indexOf(ini);
        int b = s.indexOf(fim);
        if (a < 0 || b < 0 || b <= a) return "";
        return s.substring(a + ini.length(), b).trim();
    }
}
