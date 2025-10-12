// Classe utilitária para extrair métricas do relatório do JaCoCo
package org.example.util;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.*;
import java.io.File;

public class ExtratorJacoco {
    public static void extrair(String caminhoRelatorio) {
        try {
            File xmlFile = new File(caminhoRelatorio);
            if (!xmlFile.exists()) {
                System.err.println("Arquivo não encontrado: " + xmlFile.getAbsolutePath());
                return;
            }
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setValidating(false);
            dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false); 
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);

            NodeList counters = doc.getElementsByTagName("counter");

            for (int i = 0; i < counters.getLength(); i++) {
                Element counter = (Element) counters.item(i);
                String tipo = counter.getAttribute("type");
                int missed = Integer.parseInt(counter.getAttribute("missed"));
                int covered = Integer.parseInt(counter.getAttribute("covered"));

                int total = missed + covered;
                double cobertura = total == 0 ? 0 : ((double) covered / total) * 100;

                System.out.printf("%s: %.2f%% coberto (%d/%d)\n", tipo, cobertura, covered, total);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}