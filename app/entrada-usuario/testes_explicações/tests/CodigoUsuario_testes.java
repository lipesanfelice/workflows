package org.example.generated;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CodigoUsuario_testes {

    @Test
    public void testarContagemVogais() {
        ContadorVogais.main(new String[] {"a", "e", "i", "o", "u"});
        assertEquals(5, ContadorVogais.contador);
    }

    @Test
    public void testarContagemVogaisComPalavraVazia() {
        ContadorVogais.main(new String[] {"", "", "", "", ""});
        assertEquals(0, ContadorVogais.contador);
    }

    @Test
    public void testarContagemVogaisComPalavraNaoInserida() {
        assertThrows(Exception.class, () -> ContadorVogais.main(new String[] {}));
    }

    @Test
    public void testarContagemVogaisComLetrasEspeciais() {
        ContadorVogais.main(new String[] {"a!", "e@", "i#", "o$", "u%"});
        assertEquals(5, ContadorVogais.contador);
    }

    @Test
    public void testarContagemVogaisComLetrasMinusculas() {
        ContadorVogais.main(new String[] {"a", "e", "i", "o", "u"});
        assertEquals(5, ContadorVogais.contador);
    }

    @Test
    public void testarContagemVogaisComLetrasMaiusculas() {
        ContadorVogais.main(new String[] {"A", "E", "I", "O", "U"});
        assertEquals(5, ContadorVogais.contador);
    }

    @Test
    public void testarContagemVogaisComPalavrasComEspacos() {
        ContadorVogais.main(new String[] {"a e i o u"});
        assertEquals(5, ContadorVogais.contador);
    }

    @Test
    public void testarContagemVogaisComPalavrasComAcentos() {
        ContadorVogais.main(new String[] {"á é í ó ú"});
        assertEquals(5, ContadorVogais.contador);
    }

    @Test
    public void testarContagemVogaisComPalavrasComAcentosEspeciais() {
        ContadorVogais.main(new String[] {"á!", "é@", "í#", "ó$", "ú%"});
        assertEquals(5, ContadorVogais.contador);
    }

    @Test
    public void testarContagemVogaisComPalavrasComLetrasEspeciaisEAcents() {
        ContadorVogais.main(new String[] {"á!", "é@", "í#", "ó$", "ú%"});
        assertEquals(5, ContadorVogais.contador);
    }
}