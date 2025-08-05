package org.example.util;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ExtratorJacoco {
    public static List<DadoCobertura> extrair(String caminhoRelatorio) {
        List<DadoCobertura> dados = new ArrayList<>();
        try {
            File xmlFile = new File(caminhoRelatorio);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setValidating(false);
            dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);

            NodeList classes = doc.getElementsByTagName("class");
            
            for (int i = 0; i < classes.getLength(); i++) {
                Element cls = (Element) classes.item(i);
                String className = cls.getAttribute("name");
                
                NodeList methods = cls.getElementsByTagName("method");
                for (int j = 0; j < methods.getLength(); j++) {
                    Element method = (Element) methods.item(j);
                    String methodName = method.getAttribute("name");
                    
                    NodeList counters = method.getElementsByTagName("counter");
                    for (int k = 0; k < counters.getLength(); k++) {
                        Element counter = (Element) counters.item(k);
                        if (counter.getAttribute("type").equals("LINE")) {
                            int missed = Integer.parseInt(counter.getAttribute("missed"));
                            int covered = Integer.parseInt(counter.getAttribute("covered"));
                            float cobertura = (missed + covered) == 0 ? 0 : 
                                ((float) covered / (missed + covered)) * 100;
                            
                            dados.add(new DadoCobertura(
                                className,
                                methodName,
                                -1, // linhaInicial não disponível no relatório básico
                                -1, // linhaFinal não disponível no relatório básico
                                cobertura
                            ));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dados;
    }
}