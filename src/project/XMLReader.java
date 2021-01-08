package project;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class XMLReader {
    public static XMLReader READER;
    XMLReader() {
        if (READER == null) {
            READER = this;
        } else {
            throw new RuntimeException("Cannot open xml reader");
        }
    }
    private final String PATH_TO_CONFIG = "src/configs/config.xml", PATH_TO_BACKUP = "src/configs/config.bak.xml", PATH_TO_TEXT_DATA = "src/configs/text data.xml";
    private Events.Event onConfigChanged = new Events.Event(), onTextDataChanged = new Events.Event();

    private Document document_config, document_textData;
    private DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    public boolean readConfig() {
        try {
            File file = new File(PATH_TO_CONFIG);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document_config = documentBuilder.parse(file);
            document_config.getDocumentElement().normalize();
            System.out.println("config file loaded");
            invoke();
            return true;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            System.out.println("couldn't read config file");
            return false;
        }
    }
    public boolean readTextData() {
        try {
            File file = new File(PATH_TO_TEXT_DATA);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document_textData = documentBuilder.parse(file);
            document_textData.getDocumentElement().normalize();
            System.out.println("text data file loaded");
            invoke();
            return true;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            System.out.println("couldn't read text data file");
            return false;
        }
    }
    public boolean resetConfig() {
        Transformer transformer = null;
        Document _config;
        try {
            File file = new File(PATH_TO_BACKUP);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            _config = documentBuilder.parse(file);
            _config.getDocumentElement().normalize();
            System.out.println("backup file loaded");
            invoke();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            System.out.println("couldn't read backup file");
            return false;
        }
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "5");
            DOMSource source = new DOMSource(_config);
            StreamResult result = new StreamResult(new File(PATH_TO_CONFIG));
            transformer.transform(source, result);
            readConfig();
            return true;
        } catch (TransformerException e) {
            return false;
        }
    }
    public boolean writeConfig(String confId, String newValue) {
        Transformer transformer = null;
        NodeList nodes = document_config.getElementsByTagName("parameter");
        for(int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                if (element.getAttribute("id").equals(confId)) element.setTextContent(newValue);
            }
        }
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "5");
            DOMSource source = new DOMSource(document_config);
            StreamResult result = new StreamResult(new File(PATH_TO_CONFIG));
            transformer.transform(source, result);
            return true;
        } catch (TransformerException e) {
            return false;
        }
    }
    public String getParameterById(String id) {
        StringBuilder result = new StringBuilder();
        NodeList nodes = document_config.getElementsByTagName("parameter");
        for(int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                if(element.getAttribute("id").equals(id)) result.append(element.getTextContent());
            }
        }
        //System.out.println("parameter with id " + id + ": " + result);
        return result.toString();
    }
    public String getTextById(String lcl, String id) {
        StringBuilder result = new StringBuilder();
        NodeList nodes = document_textData.getElementsByTagName("localization");
        Node node = null;
        for(int i = 0; i < nodes.getLength(); i++) {
            node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                element.normalize();
                if(element.getAttribute("id").equals(lcl)) break;
            }
        }
        NodeList values = null;
        if(node != null) {
            values = node.getChildNodes();
            for(int i = 0; i < values.getLength(); i++) {
                node = values.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    element.normalize();
                    if(element.getAttribute("id").equals(id)) result.append(element.getTextContent());
                }
            }
        }

        return result.toString();
    }
    public void subscribe(Consumer<Events.EventArgs> listener) {
        onConfigChanged.addListener(listener);
    }
    public void unsubscribe(Consumer<Events.EventArgs> listener) {
        onConfigChanged.removeListener(listener);
    }
    public void invoke() {
        onConfigChanged.broadcast(new Events.EventArgs());
    }
}
