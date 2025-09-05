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
public class ParserCodecov_testes {

    @InjectMocks
    private ParserCodecov parserCodecov;

    @Mock
    private JSONObject json;

    @Mock
    private JSONArray results;

    @Mock
    private JSONObject ultimoCommit;

    @Mock
    private JSONObject totals;

    @Test
    public void testParse_CaminhoFeliz() {
        // Setup
        when(json.getJSONArray("results")).thenReturn(results);
        when(results.getJSONObject(0)).thenReturn(ultimoCommit);
        when(ultimoCommit.getJSONObject("totals")).thenReturn(totals);
        when(totals.getDouble("coverage")).thenReturn(0.5);
        when(totals.getInt("lines")).thenReturn(100);
        when(totals.getInt("hits")).thenReturn(50);
        when(totals.getInt("methods")).thenReturn(10);
        when(ultimoCommit.getString("commitid")).thenReturn("commitid");

        // Execução
        RelatorioCobertura relatorio = parserCodecov.parse(json.toString());

        // Verificação
        assertEquals("commitid", relatorio.getNomeProjeto());
        assertEquals(1, relatorio.getClasses().size());
        assertEquals("ProjectAggregate", relatorio.getClasses().get(0).getNomeClasse());
        assertEquals("src/main/java", relatorio.getClasses().get(0).getDiretorio());
        assertEquals(1, relatorio.getClasses().get(0).getMetodos().size());
        assertEquals("metodoAgregado", relatorio.getClasses().get(0).getMetodos().get(0).getNomeMetodo());
        assertEquals(1, relatorio.getClasses().get(0).getMetodos().get(0).getLinhasCobertas());
        assertEquals(100, relatorio.getClasses().get(0).getMetodos().get(0).getLinhasTotais());
        assertEquals(0.5, relatorio.getClasses().get(0).getMetodos().get(0).getCobertura());
    }

    @Test
    public void testParse_JsonInvalido() {
        // Setup
        when(json.getJSONArray("results")).thenThrow(new JSONException("Invalid JSON"));

        // Execução e Verificação
        assertThrows(JSONException.class, () -> parserCodecov.parse(json.toString()));
    }
}