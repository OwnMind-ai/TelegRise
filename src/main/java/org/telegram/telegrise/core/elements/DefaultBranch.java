package org.telegram.telegrise.core.elements;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.elements.actions.ActionElement;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.InnerElement;

import java.util.List;

@Element(name = "default")
@Getter @Setter @NoArgsConstructor
public class DefaultBranch extends NodeElement {
    @Attribute(name = "invoke")
    private GeneratedValue<Void> toInvoke;

    @Attribute(name = "when")
    private GeneratedValue<Boolean> when = GeneratedValue.ofValue(true);

    @InnerElement
    private List<ActionElement> actions;
}
