package org.telegram.telegrise.core.elements.security;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrise.core.elements.StorableElement;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.TranscriptionMemory;
import org.telegram.telegrise.core.parser.TranscriptionParsingException;
import org.w3c.dom.Node;

@Element(name = "role")
@Data @NoArgsConstructor
public class Role implements StorableElement {
    @Attribute(name = "name", nullable = false)
    private String name;

    @Attribute(name = "accessibleTrees")
    private String[] trees;

    @Attribute(name = "level")
    private Integer level;

    @Attribute(name = "onDeniedTree")
    private String onDeniedTree;

    @Override
    public void validate(Node node, TranscriptionMemory memory) {
        if (level == null && trees == null)
            throw new TranscriptionParsingException("Either 'accessibleTrees' or 'level' must be specified", node);
    }

    @Override
    public void store(TranscriptionMemory memory) {
        memory.put(this.name, this);
    }
}
