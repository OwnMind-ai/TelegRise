package org.telegrise.telegrise.exceptions;

import org.jetbrains.annotations.NotNull;
import org.telegrise.telegrise.core.elements.base.NodeElement;
import org.w3c.dom.Node;

public class TranscriptionParsingException extends RuntimeException{
    private final Node node;

    public TranscriptionParsingException(String message, @NotNull Node problematicNode) {
        super(message, null, false, false);
        this.node = problematicNode;
    }

    private String nodeTag() {
        return NodeElement.formatNode(node);
    }

    @Override
    public String toString() {
        return "Transcription parsing error at:\n\n" + this.nodeTag() + "\n\n" + super.getMessage() + "\n";
    }
}
