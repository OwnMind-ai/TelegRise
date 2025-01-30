package org.telegrise.telegrise.core.utils;

import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
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
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;

public class XMLUtils {
    @NotNull
    public static LSSerializer getLsSerializer(Node node) {
        Document document = node.getNodeType() == Node.DOCUMENT_NODE ? (Document) node : node.getOwnerDocument();
        DOMImplementationLS ls = (DOMImplementationLS) document.getImplementation().getFeature("LS", "3.0");
        LSSerializer lsSerializer = ls.createLSSerializer();
        lsSerializer.getDomConfig().setParameter("xml-declaration", false);
        return lsSerializer;
    }

    public static String innerXML(Node node) {
        String result = extractRawInnerXML(node).stripIndent();
        if (result.isEmpty()) return null;

        String[] lines = result.split("\n");
        StringJoiner stringJoiner = new StringJoiner(" ");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            stringJoiner.add(line.trim());
        }

        return applyHTMLTextDecorators(stringJoiner.toString());
    }

    private static String extractRawInnerXML(Node node) {
        LSSerializer lsSerializer = getLsSerializer(node);

        NodeList childNodes = node.getChildNodes();
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeName().equals("#comment"))
                continue;  // Skips comments

            builder.append(lsSerializer.writeToString(childNodes.item(i)));
        }

        return builder.toString();
    }

    public static String innerXMLTextBlock(Node node){
        String raw = extractRawInnerXML(node).trim();
        if (raw.isEmpty()) return null;

        // Reasoning for this is that the first line's indentation stripped by XML parser, but the rest of the lines are not
        List<String> lines = List.of(raw.split("\n"));
        String result = lines.get(0) + "\n" + String.join("\n", lines.subList(1, lines.size())).stripIndent();

        return applyHTMLTextDecorators(result);
    }

    public static String applyHTMLTextDecorators(String string){
        return StringEscapeUtils.unescapeHtml4(string).replaceAll("<br/> ?", "\n");
    }

    public static Node[] getInstructions(Document element){
        NodeList list = element.getChildNodes();
        List<Node> result = new LinkedList<>();

        for (int i = 0; i < list.getLength(); i++)
            if (list.item(i).getNodeType() == Node.PROCESSING_INSTRUCTION_NODE)
                result.add(list.item(i));

        return result.toArray(new Node[0]);
    }

    public static Document loadDocument(File file) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newDefaultInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(file);
        document.getDocumentElement().normalize();

        return document;
    }
}
