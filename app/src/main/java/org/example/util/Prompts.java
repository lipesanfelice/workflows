package org.example.util;

public class Prompts {
    public static String montarPromptGroq(String relatorioSonarJson, String caminhoCodigo, String blocoCodigo) {
        String instrucoes = """
Gere testes unitários JUnit 5 para Java priorizando achados de maior gravidade do Sonar.
Respeite:
- Nomes de classes e métodos de teste em português
- Sem comentários no código
- Asserts determinísticos
- Mockito apenas quando necessário
- Java 21
- Um arquivo de teste por classe alvo
""";
        String contexto = ("""
===ENTRADA===
RELATORIO_SONAR_JSON:
%s

CAMINHO_BASE_CODIGO: %s

CODIGO_FONTE:
%s

REGRAS:
- Selecione classes e métodos com issues de MAJOR e CRITICAL antes das demais
- Cubra branches sinalizadas como não cobertas
- Evite acessar rede, disco ou banco
- Use pacote org.example.generated para os testes
- Importe a classe alvo pelo pacote correto quando possível
""").formatted(relatorioSonarJson, caminhoCodigo, blocoCodigo);
        String formato = """
Retorne exatamente neste formato:

====CODIGO====
<arquivo: org.example.generated.NomeClasseAlvoTest.java>
package org.example.generated;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NomeClasseAlvoTest {
    @Test
    void descricaoEmPortugues() {
    }
}
====FIM-CODIGO====
====EXPLICACAO====
{"arquivo_origem":"pacote/ClasseAlvo.java","classe_alvo":"pacote.ClasseAlvo","metodos_alvo":["metodoX"],"gravidade_max":"CRITICAL","regras_sonar":["java:SXXXX"],"lacunas_cobertura":{"linhas":[10,11,12],"branches":["condicao==true"]},"estrategia_de_teste":"caminhos felizes e de erro","casos_gerados":[{"nome":"descricaoEmPortugues","cobertura_prevista":{"linhas":[10,11,12],"branches":["condicao==true"]}}]}
====FIM-EXPLICACAO====
""";
        return instrucoes + "\n" + contexto + "\n" + formato;
    }
}
