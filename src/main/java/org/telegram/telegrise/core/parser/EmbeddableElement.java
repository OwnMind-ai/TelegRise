package org.telegram.telegrise.core.parser;

import org.telegram.telegrise.core.LocalNamespace;
import org.telegram.telegrise.core.elements.TranscriptionElement;
import org.w3c.dom.Node;

public interface EmbeddableElement extends TranscriptionElement {
    void parse(Node parent, LocalNamespace namespace);
}
