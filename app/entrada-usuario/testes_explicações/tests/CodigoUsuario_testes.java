package org.example.generated;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CodigoUsuario_testes {

    @Test
    public void testGerarSenha_CaminhoFeliz() {
        String senhaGerada = GeradorSenha.gerarSenha(12);
        assertEquals(12, senhaGerada.length());
    }

    @Test
    public void testGerarSenha_TamanhoNegativo() {
        assertThrows(IllegalArgumentException.class, () -> GeradorSenha.gerarSenha(-1));
    }

    @Test
    public void testGerarSenha_TamanhoZero() {
        assertThrows(IllegalArgumentException.class, () -> GeradorSenha.gerarSenha(0));
    }
}