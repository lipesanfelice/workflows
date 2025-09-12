package org.example.Parser;

import org.example.model.*;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ParserJacoco {

    public static RelatorioCobertura parse(File arquivoXml) {
        List<ClasseCoberta> listaClasses = new ArrayList<>();

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setValidating(false);
            dbFactory.setNamespaceAware(false);
            dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(arquivoXml);
            doc.getDocumentElement().normalize();


            NodeList classes = doc.getElementsByTagName("class");

            for (int i = 0; i < classes.getLength(); i++) {
                Element classe = (Element) classes.item(i);
                String nomeClasse = classe.getAttribute("name").replace("/", ".");
                String caminho = nomeClasse + ".java";

                List<MetodoCoberto> metodos = new ArrayList<>();
                NodeList listaMetodos = classe.getElementsByTagName("method");

                for (int j = 0; j < listaMetodos.getLength(); j++) {
                    Element metodo = (Element) listaMetodos.item(j);
                    String nomeMetodo = metodo.getAttribute("name");

                    int linhaInicio = Integer.parseInt(metodo.getAttribute("line"));
                    int linhaFim = linhaInicio + 5; // estimativa, pois o XML não tem linha final

                    double cobertura = extrairCobertura(metodo);

                    metodos.add(new MetodoCoberto(nomeMetodo, linhaInicio, linhaFim, cobertura));
                }

                listaClasses.add(new ClasseCoberta(nomeClasse, caminho, metodos));
            }

            return new RelatorioCobertura(
                    "Projeto Java (JaCoCo)",
                    listaClasses,
                    RelatorioCobertura.Ferramenta.JACOCO
            );

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static double extrairCobertura(Element metodo) {
        NodeList counters = metodo.getElementsByTagName("counter");
        for (int i = 0; i < counters.getLength(); i++) {
            Element counter = (Element) counters.item(i);
            if (counter.getAttribute("type").equals("INSTRUCTION")) {
                int covered = Integer.parseInt(counter.getAttribute("covered"));
                int missed = Integer.parseInt(counter.getAttribute("missed"));
                int total = covered + missed;
                return total == 0 ? 0 : (double) covered / total * 100;
            }
        }
        return 0;
    }
}
