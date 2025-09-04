package org.example.util;

import java.nio.file.*;
import java.util.stream.*;
import java.io.IOException;

public class LeitorCodigo {
    public static String montarBlocoCodigo(Path base, int limiteCaracteres) {
        var sb = new StringBuilder();
        try (var s = Files.walk(base)) {
            for (var p : s.filter(Files::isRegularFile).filter(f -> f.toString().endsWith(".java")).collect(Collectors.toList())) {
                var rel = base.relativize(p).toString().replace("\\", "/");
                var conteudo = Files.readString(p);
                if (sb.length() + conteudo.length() > limiteCaracteres) break;
                sb.append("===ARQUIVO===\n");
                sb.append("<path: ").append(rel).append(">\n");
                sb.append(conteudo).append("\n");
                sb.append("===FIM_ARQUIVO===\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }
}
