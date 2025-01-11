package org.telegrise.telegrise.core.elements;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegrise.telegrise.core.GeneratedValue;
import org.telegrise.telegrise.core.elements.actions.ActionElement;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.core.parser.InnerElement;

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
