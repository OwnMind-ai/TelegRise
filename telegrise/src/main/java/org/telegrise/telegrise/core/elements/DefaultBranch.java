package org.telegrise.telegrise.core.elements;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegrise.telegrise.core.elements.actions.ActionElement;
import org.telegrise.telegrise.core.elements.base.NodeElement;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.core.parser.InnerElement;

import java.util.List;

/**
 * A default branch of a tree or another branch
 * that will be chosen as the next branch if others haven't met their conditions.
 *
 * @since 0.1
 */
@Element(name = "default")
@Getter @Setter @NoArgsConstructor
public class DefaultBranch extends NodeElement {
    /**
     * An expression or method reference
     * to execute if this branch is chosen to handle an update before all action elements.
     * Equivalent to:
     * <pre>
     * {@code
     * <default ...>
     *     <invoke method="..."/>
     *     ...
     * </default>
     * }
     */
    @Attribute(name = "invoke")
    private GeneratedValue<Void> toInvoke;

    /**
     * If specified expressions result in false, action elements in the default branch will not be executed
     */
    @Attribute(name = "when")
    private GeneratedValue<Boolean> when = GeneratedValue.ofValue(true);

    @InnerElement
    private List<ActionElement> actions;
}
