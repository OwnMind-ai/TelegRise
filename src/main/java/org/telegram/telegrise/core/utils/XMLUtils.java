package org.telegram.telegrise.core.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import java.util.LinkedList;
import java.util.List;

public class XMLUtils {
    public static String innerXML(Node node){
        DOMImplementationLS ls = (DOMImplementationLS) node.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
        LSSerializer lsSerializer = ls.createLSSerializer();
        lsSerializer.getDomConfig().setParameter("xml-declaration", false);

        NodeList childNodes = node.getChildNodes();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < childNodes.getLength(); i++)
            result.append(lsSerializer.writeToString(childNodes.item(i)));

        return result.toString();
    }

    public static Node[] getInstructions(Document element){
        NodeList list = element.getChildNodes();
        List<Node> result = new LinkedList<>();

        for (int i = 0; i < list.getLength(); i++)
            if (list.item(i).getNodeType() == Node.PROCESSING_INSTRUCTION_NODE)
                result.add(list.item(i));

        return result.toArray(new Node[0]);
    }
}
