package org.telegram.telegrise.core.elements;

import org.jetbrains.annotations.NotNull;
import org.telegram.telegrise.core.utils.XMLUtils;
import org.w3c.dom.Node;
import org.w3c.dom.ls.LSSerializer;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public abstract class NodeElement {
    private Node node;

    public static @NotNull String formatNode(Node node) {
        StringBuilder builder = new StringBuilder();

        if (node.getOwnerDocument().getDocumentURI() == null){
            builder.append("<unknown source>: ");
        } else {
            try {
                builder.append(new URI(node.getOwnerDocument().getDocumentURI()).toURL().getFile()).append(": ");
            } catch (MalformedURLException | URISyntaxException e) {
                builder.append(node.getOwnerDocument().getDocumentURI()).append(": ");
            }
        }

        List<String> path = new ArrayList<>();
        traversePath(path, node.getParentNode());
        builder.append(String.join("/", path)).append("/");

        LSSerializer serializer = XMLUtils.getLsSerializer(node);
        builder.append("\n").append(serializer.writeToString(node).split("\n")[0]);

        return builder.toString();
    }

    public static void traversePath(List<String> path, Node node) {
        if (node == null || node.getNodeType() == Node.DOCUMENT_NODE) return;

        String s = node.getNodeName();
        if (node.getAttributes().getNamedItem("name") != null)
            s += "['" + node.getAttributes().getNamedItem("name").getNodeValue() + "']";

        path.add(0, s);
        traversePath(path, node.getParentNode());
    }

    public final void setElementNode(Node node){
        this.node = node;
    }

    public final Node getElementNode(){
        return node;
    }
}
