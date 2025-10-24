package org.example.generated;

====CODIGO====
public class CodigoUsuario_testes {

    @Test
    public void testJurosSimplesPositivo() {
        double capital = 1000;
        double taxa = 0.05;
        int tempo = 12;
        double resultadoEsperado = capital * taxa * tempo;
        double resultadoObtido = CalculadoraJuros.jurosSimples(capital, taxa, tempo);
        Assertions.assertEquals(resultadoEsperado, resultadoObtido, 0.0001);
    }

    @Test
    public void testJurosSimplesNegativo() {
        double capital = -1000;
        double taxa = 0.05;
        int tempo = 12;
        double resultadoEsperado = capital * taxa * tempo;
        double resultadoObtido = CalculadoraJuros.jurosSimples(capital, taxa, tempo);
        Assertions.assertEquals(resultadoEsperado, resultadoObtido, 0.0001);
    }

    @Test
    public void testJurosSimplesTempoZero() {
        double capital = 1000;
        double taxa = 0.05;
        int tempo = 0;
        double resultadoEsperado = 0;
        double resultadoObtido = CalculadoraJuros.jurosSimples(capital, taxa, tempo);
        Assertions.assertEquals(resultadoEsperado, resultadoObtido, 0.0001);
    }

    @Test
    public void testJurosCompostosPositivo() {
        double capital = 1000;
        double taxa = 0.05;
        int tempo = 12;
        double resultadoEsperado = capital * Math.pow(1 + taxa, tempo);
        double resultadoObtido = CalculadoraJuros.jurosCompostos(capital, taxa, tempo);
        Assertions.assertEquals(resultadoEsperado, resultadoObtido, 0.0001);
    }

    @Test
    public void testJurosCompostosNegativo() {
        double capital = -1000;
        double taxa = 0.05;
        int tempo = 12;
        double resultadoEsperado = capital * Math.pow(1 + taxa, tempo);
        double resultadoObtido = CalculadoraJuros.jurosCompostos(capital, taxa, tempo);
        Assertions.assertEquals(resultadoEsperado, resultadoObtido, 0.0001);
    }

    @Test
    public void testJurosCompostosTempoZero() {
        double capital = 1000;
        double taxa = 0.05;
        int tempo = 0;
        double resultadoEsperado = capital;
        double resultadoObtido = CalculadoraJuros.jurosCompostos(capital, taxa, tempo);
        Assertions.assertEquals(resultadoEsperado, resultadoObtido, 0.0001);
    }

    @Test
    public void testMontanteJurosCompostosPositivo() {
        double capital = 1000;
        double taxa = 0.05;
        int tempo = 12;
        double resultadoEsperado = capital + capital * Math.pow(1 + taxa, tempo);
        double resultadoObtido = CalculadoraJuros.montanteJurosCompostos(capital, taxa, tempo);
        Assertions.assertEquals(resultadoEsperado, resultadoObtido, 0.0001);
    }

    @Test
    public void testMontanteJurosCompostosNegativo() {
        double capital = -1000;
        double taxa = 0.05;
        int tempo = 12;
        double resultadoEsperado = capital + capital * Math.pow(1 + taxa, tempo);
        double resultadoObtido = CalculadoraJuros.montanteJurosCompostos(capital, taxa, tempo);
        Assertions.assertEquals(resultadoEsperado, resultadoObtido, 0.0001);
    }

    @Test
    public void testMontanteJurosCompostosTempoZero() {
        double capital = 1000;
        double taxa = 0.05;
        int tempo = 0;
        double resultadoEsperado = capital;
        double resultadoObtido = CalculadoraJuros.montanteJurosCompost
