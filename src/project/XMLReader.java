package project;
import javax.print.Doc;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import javafx.fxml.Initializable;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class XMLReader {
    private final String PATH_TO_CONFIG = "src/configs/config.xml";

    private Document document;
    //private String param_char, func_param_char;
    private DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    public boolean readXMLConfig() {
        try {
            File file = new File(PATH_TO_CONFIG);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(file);
            document.getDocumentElement().normalize();
            return true;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            System.out.println("config file not found");
            return false;
        }
    }
    public String getParameterById(String id) {
        String result = "";
        NodeList nodes = document.getElementsByTagName("parameter");
        for(int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                if(element.getAttribute("id").equals(id)) result = element.getTextContent();
            }
        }
        System.out.println("parameter with id " + id + ": " + result);
        return result;
    }
}
