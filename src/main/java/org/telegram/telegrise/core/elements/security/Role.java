package org.telegram.telegrise.core.elements.security;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.telegram.telegrise.core.elements.NodeElement;
import org.telegram.telegrise.core.elements.StorableElement;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.TranscriptionMemory;
import org.telegram.telegrise.exceptions.TranscriptionParsingException;

@EqualsAndHashCode(callSuper = false)
@Element(name = "role")
@Data @NoArgsConstructor
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
