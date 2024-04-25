package org.telegram.telegrise.exceptions;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Node;

public class TranscriptionParsingException extends RuntimeException{
    private final String message;
    private final Node problematicNode;

    public TranscriptionParsingException(String message, @NotNull Node problematicNode) {
        super(null, null, false, false);
        this.message = message;
        this.problematicNode = problematicNode;
    }

    private String nodeTag() {
        StringBuilder result = new StringBuilder("<" + problematicNode.getNodeName() + " ");

        if (problematicNode.getNodeType() == Node.ELEMENT_NODE){
            for (int i = 0; i < problematicNode.getAttributes().getLength(); i++) {
                Node item = problematicNode.getAttributes().item(i);
                result.append(item.getNodeName()).append("=\"").append(item.getNodeValue()).append("\" ");
            }
        } else
            result.append(problematicNode);

        return result.append(">").toString();
    }

    @Override
    public String toString() {
        return "Transcription parsing error at:\n\n" + this.nodeTag() + "\n\n" + message + "\n";
    }
}
