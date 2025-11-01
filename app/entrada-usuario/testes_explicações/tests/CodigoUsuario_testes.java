package org.example.generated;

====CODIGO====
public class CodigoUsuario_testes {

    @Test
    public void testJurosSimples() {
        Assertions.assertEquals(0, CalculadoraJuros.jurosSimples(0, 0, 0));
        Assertions.assertEquals(1000, CalculadoraJuros.jurosSimples(1000, 0, 0));
        Assertions.assertEquals(500, CalculadoraJuros.jurosSimples(1000, 0.05, 1));
        Assertions.assertEquals(600, CalculadoraJuros.jurosSimples(1000, 0.05, 2));
        Assertions.assertEquals(700, CalculadoraJuros.jurosSimples(1000, 0.05, 3));
        Assertions.assertEquals(800, CalculadoraJuros.jurosSimples(1000, 0.05, 4));
        Assertions.assertEquals(900, CalculadoraJuros.jurosSimples(1000, 0.05, 5));
        Assertions.assertEquals(1000, CalculadoraJuros.jurosSimples(1000, 0.05, 6));
        Assertions.assertEquals(1100, CalculadoraJuros.jurosSimples(1000, 0.05, 7));
        Assertions.assertEquals(1200, CalculadoraJuros.jurosSimples(1000, 0.05, 8));
        Assertions.assertEquals(1300, CalculadoraJuros.jurosSimples(1000, 0.05, 9));
        Assertions.assertEquals(1400, CalculadoraJuros.jurosSimples(1000, 0.05, 10));
        Assertions.assertEquals(1500, CalculadoraJuros.jurosSimples(1000, 0.05, 11));
        Assertions.assertEquals(1600, CalculadoraJuros.jurosSimples(1000, 0.05, 12));
    }

    @Test
    public void testJurosCompostos() {
        Assertions.assertEquals(0, CalculadoraJuros.jurosCompostos(0, 0, 0));
        Assertions.assertEquals(1000, CalculadoraJuros.jurosCompostos(1000, 0, 0));
        Assertions.assertEquals(1050, CalculadoraJuros.jurosCompostos(1000, 0.05, 1));
        Assertions.assertEquals(1102.5, CalculadoraJuros.jurosCompostos(1000, 0.05, 2));
        Assertions.assertEquals(1157.63, CalculadoraJuros.jurosCompostos(1000, 0.05, 3));
        Assertions.assertEquals(1216.49, CalculadoraJuros.jurosCompostos(1000, 0.05, 4));
        Assertions.assertEquals(1278.24, CalculadoraJuros.jurosCompostos(1000, 0.05, 5));
        Assertions.assertEquals(1343.91, CalculadoraJuros.jurosCompostos(1000, 0.05, 6));
        Assertions.assertEquals(1413.49, CalculadoraJuros.jurosCompostos(1000, 0.05, 7));
        Assertions.assertEquals(1487.03, CalculadoraJuros.jurosCompostos(1000, 0.05, 8));
        Assertions.assertEquals(1564.52, CalculadoraJuros.jurosCompostos(1000, 0.05, 9));
        Assertions.assertEquals(1646.03, CalculadoraJuros.jurosCompostos(1000, 0.05, 10));
        Assertions.assertEquals(1731.59, CalculadoraJuros.jurosCompostos(1000, 0.05, 11));
        Assertions.assertEquals(1821.24, CalculadoraJuros.jurosCompostos(1000, 0.05, 12));
    }

    @Test
    public void testMontanteJurosCompostos() {
        Assertions.assertEquals(0, CalculadoraJuros.montanteJurosCompostos(0, 0, 0));
        Assertions.assertEquals(1000, CalculadoraJuros.montanteJurosCompostos(
