package org.telegrise.telegrise.core.parser;

import org.telegrise.telegrise.core.LocalNamespace;
import org.w3c.dom.Node;

public interface EmbeddableElement {
    void parse(Node parent, LocalNamespace namespace);
}
