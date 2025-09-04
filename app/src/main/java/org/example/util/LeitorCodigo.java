package org.example.util;

import java.nio.file.*;
import java.io.IOException;

public class LeitorCodigo {
    public static String lerAteLimite(Path arquivo, int limiteCaracteres) {
        try {
            String s = Files.readString(arquivo);
            if (s.length() <= limiteCaracteres) return s;
            return s.substring(0, limiteCaracteres);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
