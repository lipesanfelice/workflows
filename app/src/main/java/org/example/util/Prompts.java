package org.example.util;

public class Prompts {
    public static String montarPromptGroqPorArquivo(String sonarRecorte, String caminhoArquivo, String conteudoArquivo) {
        String regras = """
Regras:
- Gere UM ÚNICO arquivo de teste JUnit 5 para o arquivo alvo.
- Pacote EXATO: org.example.generated
- Sem comentários, sem markdown, apenas código Java compilável.
- Nome do teste: <NomeDaClasseAlvo>Test.java
- Mockito só se necessário.
- Cubra caminho feliz e 1 caminho de erro se existir.
""";
        String contexto = ("""
ARQUIVO_ALVO: %s

CÓDIGO_ALVO:
%s

SONAR_RECORTE_RELEVANTE:
%s

Retorne SOMENTE nas marcações exigidas.
""").formatted(caminhoArquivo, conteudoArquivo, sonarRecorte);
        return regras + "\n" + contexto;
    }
}
