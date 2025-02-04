package org.telegrise.telegrise.exceptions;

import lombok.AccessLevel;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.telegrise.telegrise.core.elements.base.NodeElement;
import org.w3c.dom.Node;

public class TelegRiseRuntimeException extends RuntimeException{
    public static RuntimeException unfold(Throwable e){
        return unfold(e, null);
    }

    public static RuntimeException unfold(Throwable e, @Nullable Node node){
        if (e instanceof TelegRiseRuntimeException ex) return ex;
        if (e instanceof TranscriptionParsingException ex) return ex;
        else if (!(e instanceof TelegRiseInternalException)) return new TelegRiseRuntimeException(e.getMessage(), e, true);

        while (e.getCause() instanceof TelegRiseInternalException){
            e = e.getCause();
        }

        // After a while statement, cause should be the last TelegRiseInternalException found, so we do one step forward
        e = e.getCause();

        TelegRiseRuntimeException result;
        if (e instanceof RuntimeException) {
            result = new TelegRiseRuntimeException(e.getClass().getName() + ": " + e.getMessage(), null, true);
            result.setStackTrace(e.getStackTrace());
        } else
            result = new TelegRiseRuntimeException(null, e, true);

        if (node != null) result.setNode(node);

        return result;
    }

    @Setter(AccessLevel.PRIVATE)
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
        return node == null ? super.toString() : "Exception at node:\n\n" + NodeElement.formatNode(node) + "\n\n" + getLocalizedMessage();
    }
}
