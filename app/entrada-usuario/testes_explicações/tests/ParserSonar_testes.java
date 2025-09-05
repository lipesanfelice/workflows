package org.example.generated;

import org.example.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParserSonar_testes {

    @Mock
    private JSONObject json;

    @Mock
    private JSONObject component;

    @Mock
    private JSONArray measures;

    @Mock
    private JSONObject measure;

    @InjectMocks
    private ParserSonar parserSonar;

    @Test
    public void testParse_CaminhoFeliz() {
        // Configuração dos mocks
        when(json.getJSONObject("component")).thenReturn(component);
        when(component.getString("name")).thenReturn("app");
        when(component.getJSONArray("measures")).thenReturn(measures);
        when(measures.getJSONObject(0)).thenReturn(measure);
        when(measure.getString("metric")).thenReturn("coverage");
        when(measure.getString("value")).thenReturn("0.0");
        when(measures.getJSONObject(1)).thenReturn(measure);
        when(measure.getString("metric")).thenReturn("lines_to_cover");
        when(measure.getString("value")).thenReturn("101");
        when(measures.getJSONObject(2)).thenReturn(measure);
        when(measure.getString("metric")).thenReturn("uncovered_lines");
        when(measure.getString("value")).thenReturn("101");

        // Execução do método a ser testado
        RelatorioCobertura relatorio = parserSonar.parse(json.toString());

        // Verificação dos resultados
        assertEquals("app", relatorio.getNomeProjeto());
        assertEquals(1, relatorio.getClasses().size());
        assertEquals("app", relatorio.getClasses().get(0).getNomeClasse());
        assertEquals("src/main/java", relatorio.getClasses().get(0).getDiretorio());
        assertEquals(1, relatorio.getClasses().get(0).getMetodos().size());
        assertEquals("coberturaTotal", relatorio.getClasses().get(0).getMetodos().get(0).getNomeMetodo());
        assertEquals(1, relatorio.getClasses().get(0).getMetodos().get(0).getLinhasTotais());
        assertEquals(0.0, relatorio.getClasses().get(0).getMetodos().get(0).getCobertura());
    }

    @Test
    public void testParse_CaminhoDeErro_JsonInvalido() {
        // Configuração dos mocks
        when(json.toString()).thenThrow(new JSONException("Erro ao parsear JSON"));

        // Execução do método a ser testado
        assertThrows(JSONException.class, () -> parserSonar.parse(json.toString()));
    }
}