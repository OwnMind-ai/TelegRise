package org.telegram.telegrise.core.elements;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.elements.actions.ActionElement;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.InnerElement;

import java.util.List;

@Element(name = "default")
@Data @NoArgsConstructor
public class DefaultBranch implements TranscriptionElement {
    @Attribute(name = "handler")
    private GeneratedValue<Void> toInvoke;

    @InnerElement
    private List<ActionElement> actions;
}
