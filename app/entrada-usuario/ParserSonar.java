package org.example.Parser;

import org.example.model.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ParserSonar {

    public static RelatorioCobertura parse(String jsonResponse) {
        JSONObject json = new JSONObject(jsonResponse);
        JSONObject component = json.getJSONObject("component");
        String nomeProjeto = component.getString("name");
        
        JSONArray measures = component.getJSONArray("measures");
        double coberturaTotal = 0.0;
        int linhasTotais = 0;
        int linhasCobertas = 0;

        // Extrai métricas principais
        for (int i = 0; i < measures.length(); i++) {
            JSONObject measure = measures.getJSONObject(i);
            String metric = measure.getString("metric");
            String value = measure.getString("value");
            
            switch (metric) {
                case "coverage":
                    coberturaTotal = Double.parseDouble(value);
                    break;
                case "lines_to_cover":
                    linhasTotais = Integer.parseInt(value);
                    break;
                case "uncovered_lines":
                    linhasCobertas = linhasTotais - Integer.parseInt(value);
                    break;
            }
        }

        // Como o Sonar não fornece dados por método diretamente, simulamos uma classe
        List<ClasseCoberta> classes = new ArrayList<>();
        List<MetodoCoberto> metodos = new ArrayList<>();
        
        // Método fictício representando a cobertura geral
        metodos.add(new MetodoCoberto(
            "coberturaTotal", 
            1, 
            linhasTotais, 
            coberturaTotal
        ));
        
        classes.add(new ClasseCoberta(
            nomeProjeto, 
            "src/main/java", 
            metodos
        ));

        return new RelatorioCobertura(
            nomeProjeto,
            classes,
            RelatorioCobertura.Ferramenta.SONARQUBE
        );
    }
}