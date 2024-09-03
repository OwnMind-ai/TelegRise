package org.telegram.telegrise.core.parser;

import org.telegram.telegrise.core.LocalNamespace;
import org.w3c.dom.Node;

public interface EmbeddableElement {
    void parse(Node parent, LocalNamespace namespace);
}
