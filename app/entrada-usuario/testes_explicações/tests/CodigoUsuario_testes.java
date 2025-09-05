package org.example.generated;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.Random;
import java.util.Scanner;

public class CodigoUsuario_testes {

    @Test
    public void testCaminhoFelicidade() {
        Scanner scanner = new Scanner("5");
        Random random = new Random();
        
        int total = 0;
        for (int i = 0; i < 5; i++) {
            int dado = random.nextInt(6) + 1;
            total += dado;
        }
        
        assertEquals(15, total);
    }

    @Test
    public void testCaminhoErro_QuantidadeNegativa() {
        Scanner scanner = new Scanner("-1");
        Random random = new Random();
        
        assertThrows(Exception.class, () -> {
            int quantidade = scanner.nextInt();
            int total = 0;
            for (int i = 0; i < quantidade; i++) {
                int dado = random.nextInt(6) + 1;
                total += dado;
            }
        });
    }

    @Test
    public void testCaminhoErro_EntradaInvalida() {
        Scanner scanner = new Scanner("abc");
        Random random = new Random();
        
        assertThrows(Exception.class, scanner::nextInt);
    }
}