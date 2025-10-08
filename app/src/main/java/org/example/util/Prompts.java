
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
- Cubra o que o sonar mostra que está errado e procure possiveis erros que os testes possam ajudar.
- Maximizar a cobertura de linhas, ramos e instruções.
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

    public static String montarPromptGroqPorArquivo(String metasPriorizacao, String sonarRecorte, String caminhoArquivo, String conteudoArquivo) {
        String regras = """
Tarefa: gerar testes seguindo a prioridade obrigatória:
1) bugs → 2) vulnerabilities → 3) code_smells → 4) complexity → 5) duplicated_lines_density
Regras:
- Se uma métrica tiver valor > 0, criar testes para ela antes de passar para a próxima.
- Se todas forem 0, gerar testes prescritivos para maximizar cobertura.
- Mesmo após tratar as métricas, criar testes extras para elevar a cobertura.
- Variar caminhos, ramos, exceções, entradas inválidas e limites quando aplicável.
- Gere UM ÚNICO arquivo de teste JUnit 5 para o arquivo alvo.
- Pacote EXATO: org.example.generated
- Sem comentários, sem markdown, apenas código Java compilável.
- Nome do teste: <NomeDaClasseAlvo>Test.java
- Mockito só se necessário.
""";
        String metas = "Metas priorizadas:\n" + metasPriorizacao;
        String contexto = ("""
ARQUIVO_ALVO: %s

CÓDIGO_ALVO:
%s

SONAR_RECORTE_RELEVANTE:
%s

Retorne SOMENTE o arquivo .java de teste solicitado.
""").formatted(caminhoArquivo, conteudoArquivo, sonarRecorte);
        return regras + "\n" + metas + "\n" + contexto;
    }
}
