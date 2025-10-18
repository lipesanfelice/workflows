package org.example.web.ia;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/** Cliente mínimo para a API Chat Completions da Groq (compatível com OpenAI). */
public class GroqClient {
    private static final ObjectMapper M = new ObjectMapper();
    private final OkHttpClient http;
    private final String apiKey;

    public GroqClient(String apiKey) {
        this.apiKey = apiKey;
        this.http = new OkHttpClient.Builder()
                .callTimeout(Duration.ofSeconds(120))
                .build();
    }

    /**
     * Faz uma chamada pedindo a resposta em JSON (usando response_format: json_object).
     */
    public JsonNode chatJSON(String model, String system, String userContent) throws IOException {
        Map<String,Object> body = new HashMap<>();
        body.put("model", model);
        body.put("response_format", Map.of("type","json_object"));
        body.put("temperature", 0.2);

        var messages = new Object[] {
            Map.of("role","system", "content", system),
            Map.of("role","user",   "content", userContent)
        };
        body.put("messages", messages);

        Request req = new Request.Builder()
                .url("https://api.groq.com/openai/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(M.writeValueAsBytes(body), MediaType.parse("application/json")))
                .build();

        try (Response resp = http.newCall(req).execute()) {
            if (!resp.isSuccessful()) {
                throw new IOException("Groq HTTP " + resp.code() + ": " + (resp.body()!=null? resp.body().string():""));
            }
            JsonNode root = M.readTree(resp.body().bytes());
            String content = root.path("choices").path(0).path("message").path("content").asText("");
            if (content == null || content.isBlank()) {
                return M.createObjectNode().putArray("files"); // vazio
            }
            return M.readTree(content);
        }
    }
}
