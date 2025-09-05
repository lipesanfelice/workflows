package org.example.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.net.URL;
import java.util.Base64;

public class ExtratorSonar {

    public static String extrair(String projeto, String token) {
        try {
            String url = "https://sonarcloud.io/api/measures/component?component=" + projeto +
                    "&metricKeys=coverage,lines_to_cover,uncovered_lines,complexity,bugs,vulnerabilities,code_smells,duplicated_lines_density";
            URL obj = new URL(url);
            HttpURLConnection c = (HttpURLConnection) obj.openConnection();
            c.setRequestMethod("GET");
            String auth = token + ":";
            String encoded = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            c.setRequestProperty("Authorization", "Basic " + encoded);
            int status = c.getResponseCode();
            var in = new BufferedReader(new InputStreamReader(
                    status >= 200 && status < 300 ? c.getInputStream() : c.getErrorStream(), StandardCharsets.UTF_8));
            String s;
            StringBuilder b = new StringBuilder();
            while ((s = in.readLine()) != null) b.append(s);
            in.close();
            return b.toString();
        } catch (Exception e) {
            return "{}";
        }
    }

    public static Path extrairESalvarComRetry(String projeto, String token, Path destino, int tentativas, long esperaMs) {
        try {
            if (destino.getParent() != null) Files.createDirectories(destino.getParent());
            Path tmp = destino.resolveSibling(destino.getFileName().toString() + ".tmp");
            String conteudo = "{}";
            for (int i = 1; i <= tentativas; i++) {
                conteudo = extrair(projeto, token);
                if (conteudo != null && conteudo.length() > 2 && conteudo.contains("component")) break;
                Thread.sleep(esperaMs);
            }
            Files.writeString(tmp, conteudo, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            try {
                Files.move(tmp, destino, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (Exception ignore) {
                Files.move(tmp, destino, StandardCopyOption.REPLACE_EXISTING);
            }
            return destino;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
