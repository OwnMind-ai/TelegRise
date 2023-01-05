package org.telegram.telegrise.core.utils;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

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

}
