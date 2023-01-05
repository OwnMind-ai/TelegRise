package org.telegram.telegrise.core;

import org.w3c.dom.Node;

public class TranscriptionParsingException extends Exception{
    private final String message;
    private final Node problematicNode;

    public TranscriptionParsingException(String message, Node problematicNode) {
        this.message = message;
        this.problematicNode = problematicNode;
    }

    private String nodeTag(){
        StringBuilder result = new StringBuilder("<" + problematicNode.getNodeName() + " ");

        for (int i = 0; i < problematicNode.getAttributes().getLength(); i++) {
            Node item = problematicNode.getAttributes().item(i);
            result.append(item.getNodeName()).append("=\"").append(item.getNodeValue()).append("\" ");
        }

        return result.append(">").toString();
    }

    @Override
    public String toString() {
        return "Transcription parsing error at :\n\n" + this.nodeTag() + "\n\n" + message;
    }
}
