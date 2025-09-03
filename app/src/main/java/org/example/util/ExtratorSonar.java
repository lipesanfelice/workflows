package org.example.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
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
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            c.setRequestProperty("Authorization", "Basic " + encodedAuth);
            int status = c.getResponseCode();
            var in = new BufferedReader(new InputStreamReader(
                    status >= 200 && status < 300 ? c.getInputStream() : c.getErrorStream(),
                    StandardCharsets.UTF_8));
            String s;
            var sb = new StringBuilder();
            while ((s = in.readLine()) != null) sb.append(s);
            in.close();
            return sb.toString();
        } catch (Exception e) {
            return "{}";
        }
    }

    public static Path extrairESalvar(String projeto, String token, Path arquivoSaida) {
        try {
            String json = extrair(projeto, token);
            Path dir = arquivoSaida.getParent();
            if (dir != null) Files.createDirectories(dir);
            Path tmp = dir.resolve("." + arquivoSaida.getFileName().toString() + ".tmp");
            try (FileChannel ch = FileChannel.open(tmp,
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
                ch.write(ByteBuffer.wrap(json.getBytes(StandardCharsets.UTF_8)));
                ch.force(true);
            }
            try {
                Files.move(tmp, arquivoSaida, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (Exception e) {
                Files.move(tmp, arquivoSaida, StandardCopyOption.REPLACE_EXISTING);
            }
            return arquivoSaida;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
