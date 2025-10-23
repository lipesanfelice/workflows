package org.example.generated;

====CODIGO====
public class CodigoUsuario_testes {

    @Test
    public void testCalcularMedia_VetorVazio() {
        double[] numeros = {};
        Assertions.assertEquals(0.0, CalculadoraEstatistica.calcularMedia(numeros));
    }

    @Test
    public void testCalcularMedia_VetorComUmElemento() {
        double[] numeros = {10.5};
        Assertions.assertEquals(10.5, CalculadoraEstatistica.calcularMedia(numeros));
    }

    @Test
    public void testCalcularMedia_VetorComDoisElementos() {
        double[] numeros = {10.5, 20.3};
        Assertions.assertEquals(15.4, CalculadoraEstatistica.calcularMedia(numeros));
    }

    @Test
    public void testCalcularMedia_VetorComTresElementos() {
        double[] numeros = {10.5, 20.3, 15.7};
        Assertions.assertEquals(15.52, CalculadoraEstatistica.calcularMedia(numeros));
    }

    @Test
    public void testCalcularMedia_VetorComQuatroElementos() {
        double[] numeros = {10.5, 20.3, 15.7, 8.9};
        Assertions.assertEquals(12.38, CalculadoraEstatistica.calcularMedia(numeros));
    }

    @Test
    public void testCalcularMedia_VetorComCincoElementos() {
        double[] numeros = {10.5, 20.3, 15.7, 8.9, 25.1};
        Assertions.assertEquals(14.36, CalculadoraEstatistica.calcularMedia(numeros));
    }

    @Test
    public void testCalcularMedia_VetorComElementosNegativos() {
        double[] numeros = {-10.5, -20.3, -15.7, -8.9, -25.1};
        Assertions.assertEquals(-14.36, CalculadoraEstatistica.calcularMedia(numeros));
    }

    @Test
    public void testCalcularMedia_VetorComElementosIguais() {
        double[] numeros = {10.5, 10.5, 10.5, 10.5, 10.5};
        Assertions.assertEquals(10.5, CalculadoraEstatistica.calcularMedia(numeros));
    }

    @Test
    public void testCalcularMedia_VetorComElementosDiferentes() {
        double[] numeros = {10.5, 20.3, 15.7, 8.9, 25.1};
        Assertions.assertEquals(14.36, CalculadoraEstatistica.calcularMedia(numeros));
    }

    @Test
    public void testCalcularMediana_VetorVazio() {
        double[] numeros = {};
        Assertions.assertEquals(0.0, CalculadoraEstatistica.calcularMediana(numeros));
    }

    @Test
    public void testCalcularMediana_VetorComUmElemento() {
        double[] numeros = {10.5};
        Assertions.assertEquals(10.5, CalculadoraEstatistica.calcularMediana(numeros));
    }

    @Test
    public void testCalcularMediana_VetorComDoisElementos() {
        double[] numeros = {10.5, 20.3};
        Assertions.assertEquals(15.4, CalculadoraEstatistica.calcularMediana(numeros));
    }

    @Test
    public void testCalcularMediana_VetorComTresElementos() {
        double[] numeros = {10.5, 20.3, 15.7};
        Assertions.assertEquals(15.52, CalculadoraEstatistica.calcularMediana(numeros));
    }

    @Test
    public void testCalcularMediana_VetorComQuatroElementos() {
        double[] numeros = {10.5, 20.3, 15.7, 8.9};
        Assertions.assertEquals(12.38, CalculadoraEstatistica.calcularMediana(numeros));
    }

    @Test
    public void
