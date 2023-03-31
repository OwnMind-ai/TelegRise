package org.telegram.telegrise.core.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class XMLUtils {
    public static String innerXML(Node node){
        DOMImplementationLS ls = (DOMImplementationLS) node.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
        LSSerializer lsSerializer = ls.createLSSerializer();
        lsSerializer.getDomConfig().setParameter("xml-declaration", false);

        NodeList childNodes = node.getChildNodes();
        StringBuilder rawBuilder = new StringBuilder();
        for (int i = 0; i < childNodes.getLength(); i++)
            rawBuilder.append(lsSerializer.writeToString(childNodes.item(i)));

        String raw = rawBuilder.toString().trim();
        StringBuilder result = new StringBuilder();
        Arrays.stream(raw.split("\n")).map(String::strip).forEach(str -> result.append(str).append("\n"));

        return result.toString().trim();
    }

    public static Node[] getInstructions(Document element){
        NodeList list = element.getChildNodes();
        List<Node> result = new LinkedList<>();

        for (int i = 0; i < list.getLength(); i++)
            if (list.item(i).getNodeType() == Node.PROCESSING_INSTRUCTION_NODE)
                result.add(list.item(i));

        return result.toArray(new Node[0]);
    }

    public static Document loadDocument(File file) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(file);
        document.getDocumentElement().normalize();

        return document;
    }
}
