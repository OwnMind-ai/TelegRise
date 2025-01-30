package org.telegrise.telegrise.exceptions;

import org.telegrise.telegrise.core.elements.NodeElement;
import org.w3c.dom.Node;

public class TelegRiseRuntimeException extends RuntimeException{
    public static RuntimeException unfold(Throwable e){
        if (e instanceof TelegRiseRuntimeException ex) return ex;
        if (e instanceof TranscriptionParsingException ex) return ex;
        else if (!(e instanceof TelegRiseInternalException)) return new TelegRiseRuntimeException(e.getMessage(), e, true);

        while (e.getCause() instanceof TelegRiseInternalException){
            e = e.getCause();
        }

        // After a while statement, cause should be the last TelegRiseInternalException found, so we do one step forward
        e = e.getCause();

        return new TelegRiseRuntimeException(null, e, true);
    }

    private Node node;

    public TelegRiseRuntimeException(String message) {
        super(message);
    }

    public TelegRiseRuntimeException(String message, Node node) {
        super(message);
        this.node = node;
    }

    public TelegRiseRuntimeException(Throwable throwable, Node node) {
        super(throwable);
        this.node = node;
    }

    public TelegRiseRuntimeException(String message, Throwable throwable, boolean showStackTrace) {
        super(message, throwable, false, showStackTrace);
    }

    @Override
    public String toString() {
        return node == null ? super.toString() : "Exception at node:\n\n" + NodeElement.formatNode(node) + "\n\n" + super.toString();
    }
}
