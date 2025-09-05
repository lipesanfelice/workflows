package org.example.generated;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.Random;
import java.util.Scanner;

public class CodigoUsuario_testes {

    @Test
    public void testCaminhoFeliz() {
        Scanner scanner = new Scanner("5\n");
        Random random = new Random(0);
        
        int resultadoEsperado = 15;
        int total = 0;
        int quantidade = 5;
        
        for (int i = 0; i < quantidade; i++) {
            int dado = random.nextInt(6) + 1;
            total += dado;
        }
        
        assertEquals(resultadoEsperado, total);
    }

    @Test
    public void testScannerErro() {
        Scanner scanner = new Scanner("");
        Random random = new Random(0);
        
        assertThrows(Exception.class, () -> {
            int quantidade = scanner.nextInt();
        });
    }

    @Test
    public void testQuantidadeNegativa() {
        Scanner scanner = new Scanner("-5\n");
        Random random = new Random(0);
        
        assertThrows(Exception.class, () -> {
            int quantidade = scanner.nextInt();
        });
    }
}