package core.elements.parsers;

import core.TranscriptionParsingException;
import core.elements.TranscriptionElement;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Node;

public interface ElementParser <T extends TranscriptionElement>{
    default boolean isParseble(@NotNull Node node){
        return this.getElementName().equals(node.getNodeName());
    }

    @NotNull String getElementName();
    T parse(Node node) throws TranscriptionParsingException;
}