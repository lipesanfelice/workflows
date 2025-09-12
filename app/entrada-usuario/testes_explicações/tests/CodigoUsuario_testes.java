package org.example.generated;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CodigoUsuario_testes {

    @Test
    public void testArrayAleatorio() {
        ArrayAleatorio arrayAleatorio = new ArrayAleatorio();
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
        
        // Verifica se o array está ordenado
        assertEquals(true, Arrays.binarySearch(numeros, numeros[0]) >= 0);
        
        // Verifica se o maior valor está correto
        assertEquals(numeros[numeros.length - 1], maior);
        
        // Verifica se o menor valor está correto
        assertEquals(numeros[0], menor);
    }

    @Test
    public void testArrayAleatorioErro() {
        ArrayAleatorio arrayAleatorio = new ArrayAleatorio();
        int[] numeros = null;
        
        // Verifica se há uma exceção ao tentar ordenar um array nulo
        assertThrows(NullPointerException.class, () -> Arrays.sort(numeros));
    }
}