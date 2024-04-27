package org.telegram.telegrise.exceptions;

import org.jetbrains.annotations.NotNull;
import org.telegram.telegrise.core.utils.XMLUtils;
import org.w3c.dom.Node;
import org.w3c.dom.ls.LSSerializer;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class TranscriptionParsingException extends RuntimeException{
    private final String message;
    private final Node node;

    public TranscriptionParsingException(String message, @NotNull Node problematicNode) {
        super(null, null, false, false);
        this.message = message;
        this.node = problematicNode;
    }

    private String nodeTag() {
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
        this.traversePath(path, node.getParentNode());
        builder.append(String.join("/", path)).append("/");

        LSSerializer serializer = XMLUtils.getLsSerializer(node);
        builder.append("\n").append(serializer.writeToString(node).split("\n")[0]);

        return builder.toString();
    }

    private void traversePath(List<String> path, Node node) {
        if (node == null || node.getNodeType() == Node.DOCUMENT_NODE) return;

        String s = node.getNodeName();
        if (node.getAttributes().getNamedItem("name") != null)
            s += "['" + node.getAttributes().getNamedItem("name").getNodeValue() + "']";

        path.add(0, s);
        traversePath(path, node.getParentNode());
    }

    @Override
    public String toString() {
        return "Transcription parsing error at:\n\n" + this.nodeTag() + "\n\n" + message + "\n";
    }
}
