package core.elements.parsers;

import core.elements.TranscriptionElement;
import org.w3c.dom.Node;

public interface ElementParser {
    boolean isParseble(Node node);
    TranscriptionElement parse(Node node);
}