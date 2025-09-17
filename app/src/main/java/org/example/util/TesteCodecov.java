package org.example.util;

public class TesteCodecov {
    public static void main(String[] args) {
        String owner = "lipesanfelice";
        String repo = "workflows";
        String token = "d4fdbe2e-2160-4dd1-916d-38746a0db51b";

        ExtratorCodecov.extrair(owner, repo, token);
    }
}
