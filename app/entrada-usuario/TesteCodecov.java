package org.example.util;

public class TesteCodecov {
    public static void main(String[] args) {
        String owner = "lipesanfelice";
        String repo = "workflows";
        String token = "54705329-755b-42f7-ba92-d00741fb093c";

        ExtratorCodecov.extrair(owner, repo, token);
    }
}
