package org.example.web.ia;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.*;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClienteIaGroq implements ClienteIa {
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
    private final ObjectMapper json = new ObjectMapper();
    private final URI uri = URI.create("https://api.groq.com/openai/v1/chat/completions");
    private final String chave;
    private final String modelo;

    private static final int MAX_RETRIES = 5;
    private static final Pattern WAIT_SECONDS = Pattern.compile("try again in\\s+([0-9]+(?:\\.[0-9]+)?)s", Pattern.CASE_INSENSITIVE);

    public ClienteIaGroq(String chave, String modelo) { this.chave = chave; this.modelo = modelo; }

    @Override
    public Registro gerar(String prompt) {
        String system = """
        Você é estritamente formatado.
        Devolva EXATAMENTE UM arquivo de teste JUnit 5 para o código recebido.
        Formato OBRIGATÓRIO:
        ====CODIGO====
        <arquivo: org.example.generated.NomeDaClasseAlvoTest.java>
        ...APENAS o código Java compilável, sem comentários...
        ====FIM-CODIGO====
        ====EXPLICACAO====
        {"arquivo_origem":"..."}
        ====FIM-EXPLICACAO====
        Nada além dessas marcações deve aparecer.
        """;
        String corpo = ("""
        {"model":"%s",
         "messages":[
            {"role":"system","content":%s},
            {"role":"user","content":%s}
         ],
         "temperature":0.1,
         "max_tokens":900
        }""").formatted(modelo, jsonStr(system), jsonStr(prompt));

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
                    // Fallback: se não veio bloco, usa o conteúdo inteiro como "código"
                    if (codigo == null || codigo.isBlank()) codigo = conteudo == null ? "" : conteudo.trim();
                    return new Registro(codigo, explicacao);
                }

                if (sc == 429 && attempt < MAX_RETRIES) { dormir(sugerirEsperaMs(resp.body())); continue; }
                if (sc >= 500 && sc < 600 && attempt < MAX_RETRIES) { dormir(backoffMs(attempt)); continue; }
                throw new RuntimeException("Falha Groq: " + sc + " - " + resp.body());
            } catch (Exception e) {
                if (attempt < MAX_RETRIES && isRetriable(e)) { dormir(900L + jitter(200,600)); continue; }
                throw new RuntimeException(e);
            }
        }
    }

    private static boolean isRetriable(Exception e){ var m=e.getMessage(); return m!=null && (m.contains("java.net")||m.contains("timed out")); }
    private static long sugerirEsperaMs(String body){
        try{ var m=WAIT_SECONDS.matcher(body==null?"":body); if(m.find()){ return (long)(Double.parseDouble(m.group(1))*1000)+jitter(300,900);} }
        catch(Exception ignored){}
        return 10_000L + jitter(300,900);
    }
    private static long backoffMs(int attempt){ return (long)(Math.pow(2, attempt)*600) + jitter(200,600); }
    private static long jitter(int a,int b){ return ThreadLocalRandom.current().nextInt(a,b+1); }
    private static void dormir(long ms){ try{ Thread.sleep(ms);}catch(InterruptedException ignored){} }
    private String jsonStr(String s){ try{ return json.writeValueAsString(s);}catch(Exception e){ throw new RuntimeException(e);} }
    private String extrair(String s, String ini, String fim){
        if(s==null) return ""; int a=s.indexOf(ini), b=s.indexOf(fim); if(a<0||b<0||b<=a) return ""; return s.substring(a+ini.length(), b).trim();
    }
}
