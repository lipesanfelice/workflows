package org.example.generated;

====CODIGO====
package org.example.generated;

import org.example.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParserJacoco_testes {

    @InjectMocks
    private ParserJacoco parserJacoco;

    @Mock
    private DocumentBuilderFactory documentBuilderFactory;

    @Mock
    private DocumentBuilder documentBuilder;

    @Mock
    private Document document;

    @Mock
    private Element classe;

    @Mock
    private NodeList classes;

    @Mock
    private NodeList listaMetodos;

    @Mock
    private Element metodo;

    @Mock
    private NodeList counters;

    @Mock
    private Element counter;

    @Test
    public void testParse_CaminhoFeliz() throws Exception {
        // Configuração do mock
        when(documentBuilderFactory.newInstance()).thenReturn(documentBuilderFactory);
        when(documentBuilderFactory.newDocumentBuilder()).thenReturn(documentBuilder);
        when(documentBuilder.parse(any(File.class))).thenReturn(document);
        when(document.getDocumentElement()).thenReturn(classe);
        when(classe.getAttributes().getNamedItem("name")).thenReturn(new Attr("org.example.model.ClasseCoberta", null));
        when(classe.getElementsByTagName("method")).thenReturn(listaMetodos);
        when(listaMetodos.getLength()).thenReturn(1);
        when(listaMetodos.item(0)).thenReturn(metodo);
        when(metodo.getAttributes().getNamedItem("name")).thenReturn(new Attr("metodoCoberto", null));
        when(metodo.getAttributes().getNamedItem("line")).thenReturn(new Attr("1", null));
        when(metodo.getElementsByTagName("counter")).thenReturn(counters);
        when(counters.getLength()).thenReturn(1);
        when(counters.item(0)).thenReturn(counter);
        when(counter.getAttributes().getNamedItem("type")).thenReturn(new Attr("INSTRUCTION", null));
        when(counter.getAttributes().getNamedItem("covered")).thenReturn(new Attr("10", null));
        when(counter.getAttributes().getNamedItem("missed")).thenReturn(new Attr("0", null));

        // Execução do método a ser testado
        RelatorioCobertura relatorioCobertura = parserJacoco.parse(new File("arquivo.xml"));

        // Verificação dos resultados
        assertNotNull(relatorioCobertura);
        assertEquals("Projeto Java (JaCoCo)", relatorioCobertura.getNome());
        assertEquals(1, relatorioCobertura.getListaClasses().size());
        assertEquals("org.example.model.ClasseCoberta", relatorioCobertura.getListaClasses().get(0).getNomeClasse());
        assertEquals(1, relatorioCobertura.getListaClasses().get(0).getMetodos().size());
        assertEquals("metodoCoberto", relatorioCobertura.getListaClasses().get(0).getMetodos().get(0).getNomeMetodo());
        assertEquals(1, relatorioCobertura.getListaClasses().get(0).getMetodos().get(0).getLinhaInicio());
        assertEquals(6, relatorioCobertura.getListaClasses().get(0).getMetodos().get(0).getLinhaFim());
        assertEquals(100.0, relatorioCobertura.getListaClasses().get(0).getMetodos().get(0).getCobertura(), 0.1);
    }

    @Test
    public void testParse_CaminhoDeErro() throws Exception {
        // Configuração do mock
        when(documentBuilderFactory.newInstance()).thenThrow(ParserConfigurationException.class);

        // Execução do método a ser testado
        RelatorioCobertura relatorioCobertura = parserJacoco.parse(new File("arquivo.xml"));

        // Verificação dos resultados
