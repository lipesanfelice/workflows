package org.example.web.ia;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/** Cliente mínimo para a API Chat Completions da Groq (compatível com OpenAI). */
public class GroqClient {
    private static final ObjectMapper M = new ObjectMapper();
    private static final MediaType JSON = MediaType.get("application/json");
    private final OkHttpClient http;
    private final String apiKey;

    public GroqClient(String apiKey) {
        this.apiKey = apiKey;
        this.http = new OkHttpClient.Builder()
                .callTimeout(Duration.ofSeconds(120))
                .build();
    }

    /** Faz uma chamada pedindo a resposta em JSON (usando response_format: json_object). */
    public JsonNode chatJSON(String model, String system, String userContent) throws IOException {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("response_format", Map.of("type", "json_object"));
        body.put("temperature", 0.2);

        var messages = new Object[]{
                Map.of("role", "system", "content", system),
                Map.of("role", "user", "content", userContent)
        };
        body.put("messages", messages);

        byte[] payload = M.writeValueAsBytes(body);

        Request req = new Request.Builder()
                .url("https://api.groq.com/openai/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(payload, JSON))
                .build();

        try (Response resp = http.newCall(req).execute()) {
            if (!resp.isSuccessful()) {
                String errBody = resp.body() != null ? resp.body().string() : "";
                throw new IOException("Groq HTTP " + resp.code() + ": " + errBody);
            }
            String text = resp.body() != null ? resp.body().string() : "";
            if (text == null || text.isBlank()) {
                return M.createObjectNode().putArray("files"); // vazio
            }
            JsonNode root = M.readTree(text);
            String content = root.path("choices").path(0).path("message").path("content").asText("");
            if (content == null || content.isBlank()) {
                return M.createObjectNode().putArray("files"); // vazio
            }
            return M.readTree(content);
        }
    }
}
