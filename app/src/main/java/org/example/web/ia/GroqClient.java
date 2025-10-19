package org.example.web.ia;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/** Cliente mínimo para a API Chat Completions da Groq (compatível com OpenAI), usando java.net.http. */
public class GroqClient {
    private static final ObjectMapper M = new ObjectMapper();
    private final HttpClient http;
    private final String apiKey;

    public GroqClient(String apiKey) {
        this.apiKey = apiKey;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    /** Faz uma chamada pedindo a resposta em JSON (usando response_format: json_object). */
    public JsonNode chatJSON(String model, String system, String userContent) throws IOException {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("response_format", Map.of("type", "json_object"));
        body.put("temperature", 0.2);

        var messages = new Object[] {
                Map.of("role", "system", "content", system),
                Map.of("role", "user",   "content", userContent)
        };
        body.put("messages", messages);

        byte[] payload = M.writeValueAsBytes(body);

        HttpRequest req = HttpRequest.newBuilder(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                .timeout(Duration.ofSeconds(120))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofByteArray(payload))
                .build();

        try {
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
                throw new IOException("Groq HTTP " + resp.statusCode() + ": " + resp.body());
            }

            String text = resp.body();
            if (text == null || text.isBlank()) {
                return M.createObjectNode().putArray("files"); // vazio
            }
            JsonNode root = M.readTree(text);
            String content = root.path("choices").path(0).path("message").path("content").asText("");
            if (content == null || content.isBlank()) {
                return M.createObjectNode().putArray("files"); // vazio
            }
            return M.readTree(content);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IOException("Requisição interrompida", ie);
        }
    }
}
