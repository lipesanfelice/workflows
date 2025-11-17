package org.example.util;

public class Prompts {

    public static String montarPromptGroqPorArquivo(String sonarRecorte, String caminhoArquivo, String conteudoArquivo) {
        String regras = """
Regras:
- Gere no mínimo 20 testes.
- Gere UM ÚNICO arquivo de teste JUnit 5 para o arquivo alvo.
- Pacote EXATO: org.example.generated
- Nome exato da classe e do arquivo: <NomeDaClasseAlvo>_testes
- Retorne apenas código Java compilável, sem comentários e sem markdown.
- Usar somente JUnit 5 (org.junit.jupiter.api.Test e Assertions estáticas).
- Não usar Mockito, reflexão, threads, temporizadores, aleatoriedade, System.in/out, System.exit, rede, acesso a arquivos, UI ou qualquer I/O externo.
- Não alterar o código de produção.
- Maximizar cobertura de linhas, ramos e exceções quando aplicável.
""";
        String contexto = ("""
ARQUIVO_ALVO: %s

CÓDIGO_ALVO:
%s

SONAR_RECORTE_RELEVANTE:
%s

Saída esperada:
Código Java compilável de uma classe pública org.example.generated.<NomeDaClasseAlvo>_testes.
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
- Os testes DEVEM rodar sob instrumentação do JaCoCo sem erros.
- Após cobrir as métricas, se fizer sentido, criar testes adicionais para elevar a cobertura.
- Variar caminhos, ramos, exceções, entradas inválidas e limites quando aplicável.
- Gere UM ÚNICO arquivo de teste JUnit 5 para o arquivo alvo.
- Pacote EXATO: org.example.generated
- Nome exato da classe e do arquivo: <NomeDaClasseAlvo>_testes
- Retorne apenas código Java compilável, sem comentários e sem markdown.
- Usar somente JUnit 5 (org.junit.jupiter.api.Test e Assertions estáticas).
- Não usar Mockito, reflexão, threads, temporizadores, aleatoriedade, System.in/out, System.exit, rede, acesso a arquivos, UI ou qualquer I/O externo.
- Não alterar o código de produção.
""";
        String metas = "Metas priorizadas (texto):\n" + (metasPriorizacao == null ? "" : metasPriorizacao);
        String contexto = ("""
ARQUIVO_ALVO: %s

CÓDIGO_ALVO:
%s

SONAR_RECORTE_RELEVANTE:
%s

Saída esperada:
Código Java compilável de uma classe pública org.example.generated.<NomeDaClasseAlvo>_testes.
""").formatted(caminhoArquivo, conteudoArquivo, sonarRecorte);
        return regras + "\n" + metas + "\n" + contexto;
    }
}
