package org.telegram.telegrise.exceptions;

import org.jetbrains.annotations.NotNull;
import org.telegram.telegrise.core.elements.NodeElement;
import org.w3c.dom.Node;

public class TranscriptionParsingException extends RuntimeException{
    private final String message;
    private final Node node;

    public TranscriptionParsingException(String message, @NotNull Node problematicNode) {
        super(null, null, false, false);
        this.message = message;
        this.node = problematicNode;
    }

    private String nodeTag() {
        return NodeElement.formatNode(node);
    }

    @Override
    public String toString() {
        return "Transcription parsing error at:\n\n" + this.nodeTag() + "\n\n" + message + "\n";
    }
}
