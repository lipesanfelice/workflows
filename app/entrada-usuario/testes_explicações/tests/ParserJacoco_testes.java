package org.example.generated;

import org.example.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParserJacoco_testes {

    @Mock
    private DocumentBuilderFactory documentBuilderFactory;

    @Mock
    private DocumentBuilder documentBuilder;

    @InjectMocks
    private ParserJacoco parserJacoco;

    @Test
    public void testParse_CaminhoFeliz() throws Exception {
        // Cria um arquivo XML exemplo
        String xml = "<root><class name=\"org.example.Exemplo\"><method name=\"exemplo\" line=\"10\"/></class></root>";
        File arquivoXml = new File("exemplo.xml");
        Files.write(Paths.get(arquivoXml.getAbsolutePath()), xml.getBytes());

        // Simula o comportamento do DocumentBuilderFactory e DocumentBuilder
        when(documentBuilderFactory.newInstance()).thenReturn(documentBuilderFactory);
        when(documentBuilderFactory.newDocumentBuilder()).thenReturn(documentBuilder);
        when(documentBuilder.parse(arquivoXml)).thenReturn(new org.w3c.dom.Document());

        // Executa o método a ser testado
        RelatorioCobertura relatorio = parserJacoco.parse(arquivoXml);

        // Verifica se o método retornou um objeto não nulo
        assertNotNull(relatorio);

        // Verifica se o relatório contém a classe e método esperados
        assertEquals("org.example.Exemplo", relatorio.getListaClasses().get(0).getNomeClasse());
        assertEquals("exemplo", relatorio.getListaClasses().get(0).getMetodos().get(0).getNomeMetodo());
    }

    @Test
    public void testParse_CaminhoDeErro() throws Exception {
        // Cria um arquivo XML inválido
        File arquivoXml = new File("exemplo.xml");
        Files.write(Paths.get(arquivoXml.getAbsolutePath()), "".getBytes());

        // Executa o método a ser testado
        RelatorioCobertura relatorio = parserJacoco.parse(arquivoXml);

        // Verifica se o método retornou um objeto nulo
        assertEquals(null, relatorio);
    }
}