package org.example.Parser;

import org.example.model.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ParserCodecov {

    public static RelatorioCobertura parse(String jsonResponse) {
        JSONObject json = new JSONObject(jsonResponse);
        JSONArray results = json.getJSONArray("results");
        
        // Pega o commit mais recente
        JSONObject ultimoCommit = results.getJSONObject(0);
        JSONObject totals = ultimoCommit.getJSONObject("totals");
        
        double coberturaTotal = totals.getDouble("coverage");
        int linhasTotais = totals.getInt("lines");
        int linhasCobertas = totals.getInt("hits");
        int metodosTotais = totals.getInt("methods");
        
        String nomeProjeto = ultimoCommit.getString("commitid");

        List<ClasseCoberta> classes = new ArrayList<>();
        List<MetodoCoberto> metodos = new ArrayList<>();
        
        // Como o Codecov não fornece detalhes por método, criamos um agregado
        metodos.add(new MetodoCoberto(
            "metodoAgregado", 
            1, 
            linhasTotais, 
            coberturaTotal
        ));
        
        classes.add(new ClasseCoberta(
            "ProjectAggregate",
            "src/main/java",
            metodos
        ));

        return new RelatorioCobertura(
            nomeProjeto,
            classes,
            RelatorioCobertura.Ferramenta.CODECOV
        );
    }
}