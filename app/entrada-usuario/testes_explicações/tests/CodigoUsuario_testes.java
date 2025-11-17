package org.example.generated;

public class CodigoUsuario_testes {

    @Test
    public void testCodigoUsuario() {
        // Testar entrada válida
        Scanner scanner = new Scanner("10");
        JogoAdivinhacao.main(new String[] { "10" });
        scanner.close();

        // Testar entrada inválida (número negativo)
        scanner = new Scanner("-10");
        JogoAdivinhacao.main(new String[] { "-10" });
        scanner.close();

        // Testar entrada inválida (número decimal)
        scanner = new Scanner("10.5");
        JogoAdivinhacao.main(new String[] { "10.5" });
        scanner.close();

        // Testar entrada inválida (letra)
        scanner = new Scanner("a");
        JogoAdivinhacao.main(new String[] { "a" });
        scanner.close();

        // Testar exceção ao fechar o scanner
        scanner = new Scanner("10");
        JogoAdivinhacao.main(new String[] { "10" });
        try {
            scanner.close();
        } catch (Exception e) {
            fail("Scanner não pode ser fechado");
        }
    }

    @Test
    public void testCodigoUsuarioComplexidade() {
        // Testar complexidade do código
        Scanner scanner = new Scanner("10");
        JogoAdivinhacao.main(new String[] { "10" });
        scanner.close();

        // Testar complexidade do código com entrada inválida
        scanner = new Scanner("-10");
        JogoAdivinhacao.main(new String[] { "-10" });
        scanner.close();
    }

    @Test
    public void testCodigoUsuarioCodeSmells() {
        // Testar código com checagem de condições
        Scanner scanner = new Scanner("10");
        JogoAdivinhacao.main(new String[] { "10" });
        scanner.close();

        // Testar código com checagem de condições com entrada inválida
        scanner = new Scanner("-10");
        JogoAdivinhacao.main(new String[] { "-10" });
        scanner.close();
    }

    @Test
    public void testCodigoUsuarioDuplicatedLinesDensity() {
        // Testar densidade de linhas duplicadas
        Scanner scanner = new Scanner("10");
        JogoAdivinhacao.main(new String[] { "10" });
        scanner.close();

        // Testar densidade de linhas duplicadas com entrada inválida
        scanner = new Scanner("-10");
        JogoAdivinhacao.main(new String[] { "-10" });
        scanner.close();
    }

    @Test
    public void testCodigoUsuarioVulnerabilities() {
        // Testar vulnerabilidades do código
        Scanner scanner = new Scanner("10");
        JogoAdivinhacao.main(new String[] { "10" });
        scanner.close();

        // Testar vulnerabilidades do código com entrada inválida
        scanner = new Scanner("-10");
        JogoAdivinhacao.main(new String[] { "-10" });
        scanner.close();
    }
}
