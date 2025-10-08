
package org.example.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public final class PriorizadorSonar {
    public static PlanoTestes gerarPlano(String jsonSonar) {
        Map<String, String> medidas = extrairMedidas(jsonSonar);
        BigDecimal bugs = valor(medidas.getOrDefault("bugs","0"));
        BigDecimal vulns = valor(medidas.getOrDefault("vulnerabilities","0"));
        BigDecimal smells = valor(medidas.getOrDefault("code_smells","0"));
        BigDecimal complex = valor(medidas.getOrDefault("complexity","0"));
        BigDecimal dup = valor(medidas.getOrDefault("duplicated_lines_density","0"));
        BigDecimal cobertura = valor(medidas.getOrDefault("coverage", "-1"));
        BigDecimal linhasCobrir = valor(medidas.getOrDefault("lines_to_cover", "-1"));
        BigDecimal linhasDescobertas = valor(medidas.getOrDefault("uncovered_lines", "-1"));

        List<ItemPlano> itens = new ArrayList<>();
        if (bugs.compareTo(BigDecimal.ZERO) > 0) itens.add(ItemPlano.de("bugs", bugs.intValue(), "Criar testes que reproduzam e evitem regressão dos defeitos."));
        if (vulns.compareTo(BigDecimal.ZERO) > 0) itens.add(ItemPlano.de("vulnerabilities", vulns.intValue(), "Criar testes que exercitem caminhos sensíveis e quebrem exploração de vulnerabilidades."));
        if (smells.compareTo(BigDecimal.ZERO) > 0) itens.add(ItemPlano.de("code_smells", smells.intValue(), "Criar testes que forcem refatorações seguras e validem comportamento em trechos com smells."));
        if (complex.compareTo(BigDecimal.ZERO) > 0) itens.add(ItemPlano.de("complexity", complex.intValue(), "Criar testes focados em ramos e caminhos de maior complexidade."));
        if (dup.compareTo(BigDecimal.ZERO) > 0) itens.add(ItemPlano.de("duplicated_lines_density", dup.intValue(), "Criar testes que cubram variantes equivalentes para reduzir divergência."));

        boolean todasZeradas = bugs.signum()==0 && vulns.signum()==0 && smells.signum()==0 && complex.signum()==0 && dup.signum()==0;

        boolean precisaCobertura;
        if (cobertura.compareTo(BigDecimal.valueOf(-1))>0) {
            precisaCobertura = cobertura.compareTo(BigDecimal.valueOf(100)) < 0;
        } else if (linhasCobrir.signum()>=0 && linhasDescobertas.signum()>=0) {
            precisaCobertura = linhasDescobertas.compareTo(BigDecimal.ZERO) > 0;
        } else {
            precisaCobertura = true;
        }

        if (todasZeradas) itens.add(ItemPlano.de("coverage_prescritiva", 0, "Criar bateria de testes prescritivos para maximizar cobertura (classes, métodos, ramos, exceções)."));
        if (precisaCobertura) itens.add(ItemPlano.de("coverage_reforco", 0, "Após cobrir as métricas, criar testes adicionais para aumentar cobertura de linhas, ramos e casos de erro."));

        return new PlanoTestes(medidas, ordenar(itens));
    }

    public static String metasComoTexto(PlanoTestes plano) {
        return plano.itens().stream()
                .map(i -> "- alvo: " + i.alvo() + " | quantidade: " + i.quantidade() + " | diretriz: " + i.diretriz())
                .collect(Collectors.joining("\n"));
    }

    private static Map<String, String> extrairMedidas(String json) {
        try {
            ObjectMapper m = new ObjectMapper();
            JsonNode raiz = m.readTree(json);
            JsonNode arr = raiz.path("component").path("measures");
            Map<String,String> out = new HashMap<>();
            if (arr.isArray()) {
                for (JsonNode n : arr) {
                    String met = n.path("metric").asText("");
                    String val = n.path("value").asText("0");
                    if (!met.isEmpty()) out.put(met, val);
                }
            }
            return out;
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private static BigDecimal valor(String s) {
        try { return new BigDecimal(s); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private static List<ItemPlano> ordenar(List<ItemPlano> itens) {
        Map<String,Integer> peso = new HashMap<>();
        peso.put("bugs", 1);
        peso.put("vulnerabilities", 2);
        peso.put("code_smells", 3);
        peso.put("complexity", 4);
        peso.put("duplicated_lines_density", 5);
        peso.put("coverage_prescritiva", 6);
        peso.put("coverage_reforco", 7);
        itens.sort(Comparator.comparingInt(i -> peso.getOrDefault(i.alvo(), 99)));
        return itens;
    }

    public static final class ItemPlano {
        private final String alvo;
        private final int quantidade;
        private final String diretriz;
        private ItemPlano(String alvo, int quantidade, String diretriz) { this.alvo = alvo; this.quantidade = quantidade; this.diretriz = diretriz; }
        public static ItemPlano de(String alvo, int quantidade, String diretriz) { return new ItemPlano(alvo, quantidade, diretriz); }
        public String alvo() { return alvo; }
        public int quantidade() { return quantidade; }
        public String diretriz() { return diretriz; }
    }

    public static final class PlanoTestes {
        private final Map<String,String> medidas;
        private final List<ItemPlano> itens;
        public PlanoTestes(Map<String,String> medidas, List<ItemPlano> itens) { this.medidas = medidas; this.itens = itens; }
        public Map<String,String> medidas() { return medidas; }
        public List<ItemPlano> itens() { return itens; }
    }
}
