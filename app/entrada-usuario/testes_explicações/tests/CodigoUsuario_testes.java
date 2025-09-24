package org.example.generated;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.Arrays;
import java.util.Random;

public class CodigoUsuario_testes {

    @Test
    public void testPreencherArrayAleatorio() {
        int[] numeros = new int[10];
        Random random = new Random();
        
        // Preenche array com valores aleatórios
        for (int i = 0; i < numeros.length; i++) {
            numeros[i] = random.nextInt(1000);
        }
        
        // Verifica se o array tem a quantidade correta de elementos
        assertEquals(10, numeros.length);
    }

    @Test
    public void testOrdenarArray() {
        int[] numeros = {5, 2, 8, 1, 9};
        Arrays.sort(numeros);
        
        // Verifica se o array está ordenado
        assertEquals(1, numeros[0]);
        assertEquals(2, numeros[1]);
        assertEquals(5, numeros[2]);
        assertEquals(8, numeros[3]);
        assertEquals(9, numeros[4]);
    }

    @Test
    public void testEncontrarMaiorEMenorValor() {
        int[] numeros = {5, 2, 8, 1, 9};
        Arrays.sort(numeros);
        
        // Encontra maior e menor valor
        int maior = numeros[numeros.length - 1];
        int menor = numeros[0];
        
        // Verifica se o maior valor é o último elemento do array
        assertEquals(9, maior);
        
        // Verifica se o menor valor é o primeiro elemento do array
        assertEquals(1, menor);
    }

    @Test
    public void testArrayAleatorio() {
        int[] numeros = new int[10];
        Random random = new Random();
        
        // Preenche array com valores aleatórios
        for (int i = 0; i < numeros.length; i++) {
            numeros[i] = random.nextInt(1000);
        }
        
        // Ordena o array
        Arrays.sort(numeros);
        
        // Encontra maior e menor valor
        int maior = numeros[numeros.length - 1];
        int menor = numeros[0];
        
        // Verifica se o array tem a quantidade correta de elementos
        assertEquals(10, numeros.length);
        
        // Verifica se o maior valor é o último elemento do array
        assertEquals(maior, numeros[numeros.length - 1]);
        
        // Verifica se o menor valor é o primeiro elemento do array
        assertEquals(menor, numeros[0]);
    }
}