package org.example.generated;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CodigoUsuario_testes {

    @Test
    public void testarCalculoIMC() {
        double peso = 70;
        double altura = 1.75;
        double imc = CalculadoraIMC.calcularIMC(peso, altura);
        assertEquals(22.86, imc, 0.01);
    }

    @Test
    public void testarClassificacaoIMCAbaixoDoPeso() {
        double imc = 17;
        String classificacao = CalculadoraIMC.classificarIMC(imc);
        assertEquals("Abaixo do peso", classificacao);
    }

    @Test
    public void testarClassificacaoIMCPesoNormal() {
        double imc = 22.5;
        String classificacao = CalculadoraIMC.classificarIMC(imc);
        assertEquals("Peso normal", classificacao);
    }

    @Test
    public void testarClassificacaoIMCSobrepeso() {
        double imc = 27.5;
        String classificacao = CalculadoraIMC.classificarIMC(imc);
        assertEquals("Sobrepeso", classificacao);
    }

    @Test
    public void testarClassificacaoIMCObesidadeGrauI() {
        double imc = 32.5;
        String classificacao = CalculadoraIMC.classificarIMC(imc);
        assertEquals("Obesidade grau I", classificacao);
    }

    @Test
    public void testarClassificacaoIMCObesidadeGrauII() {
        double imc = 37.5;
        String classificacao = CalculadoraIMC.classificarIMC(imc);
        assertEquals("Obesidade grau II", classificacao);
    }

    @Test
    public void testarClassificacaoIMCObesidadeGrauIII() {
        double imc = 42.5;
        String classificacao = CalculadoraIMC.classificarIMC(imc);
        assertEquals("Obesidade grau III", classificacao);
    }

    @Test
    public void testarCalculoIMCComZero() {
        double peso = 0;
        double altura = 1.75;
        assertThrows(ArithmeticException.class, () -> CalculadoraIMC.calcularIMC(peso, altura));
    }

    @Test
    public void testarCalculoIMCComAlturaNegativa() {
        double peso = 70;
        double altura = -1.75;
        assertThrows(ArithmeticException.class, () -> CalculadoraIMC.calcularIMC(peso, altura));
    }

    @Test
    public void testarCalculoIMCComPesoNegativo() {
        double peso = -70;
        double altura = 1.75;
        assertThrows(ArithmeticException.class, () -> CalculadoraIMC.calcularIMC(peso, altura));
    }

    @Test
    public void testarClassificacaoIMCComIMCNegativo() {
        double imc = -1;
        assertThrows(ArithmeticException.class, () -> CalculadoraIMC.classificarIMC(imc));
    }
}