package org.example.generated;

====CODIGO====
public class CodigoUsuario_testes {

    @Test
    public void testMetroParaCentimetro() {
        Assertions.assertEquals(200, ConversorUnidades.metroParaCentimetro(2));
        Assertions.assertEquals(0, ConversorUnidades.metroParaCentimetro(0));
        Assertions.assertEquals(Double.POSITIVE_INFINITY, ConversorUnidades.metroParaCentimetro(Double.POSITIVE_INFINITY));
        Assertions.assertEquals(Double.NEGATIVE_INFINITY, ConversorUnidades.metroParaCentimetro(Double.NEGATIVE_INFINITY));
    }

    @Test
    public void testCentimetroParaMetro() {
        Assertions.assertEquals(1.5, ConversorUnidades.centimetroParaMetro(150), 0.01);
        Assertions.assertEquals(0, ConversorUnidades.centimetroParaMetro(0));
        Assertions.assertEquals(Double.POSITIVE_INFINITY, ConversorUnidades.centimetroParaMetro(Double.POSITIVE_INFINITY));
        Assertions.assertEquals(Double.NEGATIVE_INFINITY, ConversorUnidades.centimetroParaMetro(Double.NEGATIVE_INFINITY));
    }

    @Test
    public void testKilmetroParaMetro() {
        Assertions.assertEquals(3500, ConversorUnidades.kilmetroParaMetro(3.5));
        Assertions.assertEquals(0, ConversorUnidades.kilmetroParaMetro(0));
        Assertions.assertEquals(Double.POSITIVE_INFINITY, ConversorUnidades.kilmetroParaMetro(Double.POSITIVE_INFINITY));
        Assertions.assertEquals(Double.NEGATIVE_INFINITY, ConversorUnidades.kilmetroParaMetro(Double.NEGATIVE_INFINITY));
    }

    @Test
    public void testLitroParaMililitro() {
        Assertions.assertEquals(2000, ConversorUnidades.litroParaMililitro(2));
        Assertions.assertEquals(0, ConversorUnidades.litroParaMililitro(0));
        Assertions.assertEquals(Double.POSITIVE_INFINITY, ConversorUnidades.litroParaMililitro(Double.POSITIVE_INFINITY));
        Assertions.assertEquals(Double.NEGATIVE_INFINITY, ConversorUnidades.litroParaMililitro(Double.NEGATIVE_INFINITY));
    }

    @Test
    public void testMain() {
        Assertions.assertEquals("2 metros em centímetros: 200.0\n150 cm em metros: 1.5\n3.5 km em metros: 3500.0\n2 litros em ml: 2000.0", ConversorUnidades.main(new String[] {}));
    }

    @Test
    public void testMetroParaCentimetroNegativo() {
        Assertions.assertEquals(-200, ConversorUnidades.metroParaCentimetro(-2));
    }

    @Test
    public void testCentimetroParaMetroNegativo() {
        Assertions.assertEquals(-1.5, ConversorUnidades.centimetroParaMetro(-150), 0.01);
    }

    @Test
    public void testKilmetroParaMetroNegativo() {
        Assertions.assertEquals(-3500, ConversorUnidades.kilmetroParaMetro(-3.5));
    }

    @Test
    public void testLitroParaMililitroNegativo() {
        Assertions.assertEquals(-2000, ConversorUnidades.litroParaMililitro(-2));
    }

    @Test
    public void testMetroParaCentimetroZero() {
        Assertions.assertEquals(0, ConversorUnidades.metroParaCentimetro(0));
    }

    @Test
    public void testCentimetroParaMetroZero() {
        Assertions.assertEquals(0, ConversorUnidades.centimetroParaMetro(0));
    }

    @Test
    public void testKilmetroParaMetroZero() {
        Assertions.assertEquals(0, ConversorUnidades.kilmetroParaMetro(0));
    }

    @Test
    public void testLitroParaMililitroZero() {
        Assertions.assertEquals(0, ConversorUnidades.litroParaMililitro(0));
    }

    @Test
    public void testMetroParaCentimetroNulo() {
        Assertions.assertThrows(NullPointerException.class, () -> ConversorUnidades.metroParaCentimetro(null));
    }

    @Test
    public void testCentimetroParaMetroNulo() {
        Assertions.assertThrows(NullPointerException.class, () -> ConversorUnidades.centimetroParaMetro(null));
    }

    @Test
    public void testK
