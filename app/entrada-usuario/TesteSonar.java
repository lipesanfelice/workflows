package org.example.util;

public class TesteSonar {
    public static void main(String[] args) {
        // Substitua por seu token e chave do projeto no SonarCloud
        String projeto = "lipesanfelice_workflows"; // exemplo: "felipesanfelice_projeto-teste"
        String token = "869a0095d5eeff251ea1d9ea8bdd34cc9e4cbe38";

        ExtratorSonar.extrair(projeto, token);
    }
}
