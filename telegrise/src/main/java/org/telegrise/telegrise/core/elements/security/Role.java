package org.telegrise.telegrise.core.elements.security;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegrise.telegrise.core.elements.NodeElement;
import org.telegrise.telegrise.core.elements.StorableElement;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.core.parser.TranscriptionMemory;
import org.telegrise.telegrise.exceptions.TranscriptionParsingException;

@Element(name = "role")
@Getter @Setter @NoArgsConstructor
public class Role extends NodeElement implements StorableElement {
    @Attribute(name = "name", nullable = false)
    private String name;

    @Attribute(name = "accessibleTrees")
    private String[] trees;

    @Attribute(name = "level")
    private Integer level;

    @Attribute(name = "onDeniedTree")
    private String onDeniedTree;

    @Override
    public void validate(TranscriptionMemory memory) {
        if (level == null && trees == null)
            throw new TranscriptionParsingException("Either 'accessibleTrees' or 'level' must be specified", node);
    }

    @Override
    public void store(TranscriptionMemory memory) {
        memory.put(parentTree, this.name, this);
    }
}
