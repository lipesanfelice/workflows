package org.example.util;

public class Prompts {
    public static String montarPromptGroqPorArquivo(String sonarRecorte, String caminhoArquivo, String conteudoArquivo) {
        String instrucoes = """
Gere testes unitários JUnit 5 para o arquivo Java fornecido.
Priorize problemas de maior gravidade presentes no recorte do relatório Sonar.
Regras:
- Escreva testes em pacote org.example.generated
- Um arquivo de teste por classe alvo
- Sem comentários no código
- Mockito apenas quando necessário
- Java 21, JUnit 5
- Cubra caminhos felizes e de erro quando fizer sentido
- Não inclua blocos ``` ou cercas de código
- Use nomes de métodos de teste em português
""";
        String contexto = ("""
===ARQUIVO_ALVO===
CAMINHO: %s

CÓDIGO:
%s

===SONAR_RECORTE_RELACIONADO_AO_ARQUIVO===
%s

Formato de resposta (EXATAMENTE assim):
====CODIGO====
<arquivo: org.example.generated.NomeDaClasseAlvoTest.java>
package org.example.generated;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NomeDaClasseAlvoTest {
    @Test
    void deveExecutarCaminhoPrincipal() {
    }
}
====FIM-CODIGO====
====EXPLICACAO====
{"arquivo_origem":"%s","classe_alvo":"pacote.ClasseAlvo","gravidade_max":"CRITICAL","regras_sonar":["java:SXXXX"],"casos_gerados":[{"nome":"deveExecutarCaminhoPrincipal"}]}
====FIM-EXPLICACAO====
""").formatted(caminhoArquivo, conteudoArquivo, sonarRecorte, caminhoArquivo);
        return instrucoes + "\n" + contexto;
    }
}
