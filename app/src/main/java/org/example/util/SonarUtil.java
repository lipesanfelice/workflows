package org.example.util;

public class SonarUtil {
    // Heurística simples: tenta localizar o nome do arquivo no JSON e recorta um trecho ao redor.
    public static String extrairTrechoPorArquivo(String sonarJson, String nomeArquivo, int maxChars) {
        if (sonarJson == null) return "";
        String sj = sonarJson;
        int idx = -1;
        // tenta por nome exato
        if (nomeArquivo != null) {
            idx = sj.indexOf(nomeArquivo);
        }
        // fallback: só corta o início
        if (idx < 0) {
            return sj.length() <= maxChars ? sj : sj.substring(0, maxChars);
        }
        int window = Math.max(maxChars, 1000);
        int start = Math.max(0, idx - window/2);
        int end   = Math.min(sj.length(), start + window);
        String recorte = sj.substring(start, end);
        return recorte.length() <= maxChars ? recorte : recorte.substring(0, maxChars);
    }
}
