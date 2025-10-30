package org.example.generated;

public class CodigoUsuario_testes {

    @Test
    public void testAreaCirculoPositivo() {
        double raio = 5;
        double resultadoEsperado = Math.PI * raio * raio;
        double resultadoCalculado = CalculadoraGeometrica.areaCirculo(raio);
        Assertions.assertEquals(resultadoEsperado, resultadoCalculado, 0.00001);
    }

    @Test
    public void testAreaCirculoNegativo() {
        double raio = -5;
        Assertions.assertThrows(ArithmeticException.class, () -> CalculadoraGeometrica.areaCirculo(raio));
    }

    @Test
    public void testAreaCirculoZero() {
        double raio = 0;
        double resultadoEsperado = 0;
        double resultadoCalculado = CalculadoraGeometrica.areaCirculo(raio);
        Assertions.assertEquals(resultadoEsperado, resultadoCalculado);
    }

    @Test
    public void testAreaRetanguloPositivo() {
        double base = 4;
        double altura = 6;
        double resultadoEsperado = base * altura;
        double resultadoCalculado = CalculadoraGeometrica.areaRetangulo(base, altura);
        Assertions.assertEquals(resultadoEsperado, resultadoCalculado);
    }

    @Test
    public void testAreaRetanguloNegativo() {
        double base = -4;
        double altura = 6;
        Assertions.assertThrows(ArithmeticException.class, () -> CalculadoraGeometrica.areaRetangulo(base, altura));
    }

    @Test
    public void testAreaRetanguloZero() {
        double base = 0;
        double altura = 6;
        double resultadoEsperado = 0;
        double resultadoCalculado = CalculadoraGeometrica.areaRetangulo(base, altura);
        Assertions.assertEquals(resultadoEsperado, resultadoCalculado);
    }

    @Test
    public void testAreaTrianguloPositivo() {
        double base = 3;
        double altura = 4;
        double resultadoEsperado = (base * altura) / 2;
        double resultadoCalculado = CalculadoraGeometrica.areaTriangulo(base, altura);
        Assertions.assertEquals(resultadoEsperado, resultadoCalculado);
    }

    @Test
    public void testAreaTrianguloNegativo() {
        double base = -3;
        double altura = 4;
        Assertions.assertThrows(ArithmeticException.class, () -> CalculadoraGeometrica.areaTriangulo(base, altura));
    }

    @Test
    public void testAreaTrianguloZero() {
        double base = 0;
        double altura = 4;
        double resultadoEsperado = 0;
        double resultadoCalculado = CalculadoraGeometrica.areaTriangulo(base, altura);
        Assertions.assertEquals(resultadoEsperado, resultadoCalculado);
    }

    @Test
    public void testMain() {
        String[] args = {};
        CalculadoraGeometrica.main(args);
    }
}
