package org.telegram.telegrise.core.elements;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.elements.actions.ActionElement;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.InnerElement;
import org.telegram.telegrise.core.parser.TranscriptionParsingException;
import org.w3c.dom.Node;

import java.util.List;

@Element(name = "branch")
@Data
@NoArgsConstructor
public class Branch implements TranscriptionElement{
    @Attribute(name = "when")
    private GeneratedValue<Boolean> when;

    @Attribute(name = "keys")
    private String[] keys;
    @Attribute(name = "callbackTriggers")
    private String[] callbackTriggers;

    @Attribute(name = "invoke")
    private GeneratedValue<Void> toInvoke;

    @InnerElement
    private List<ActionElement> actions;

    @InnerElement
    private List<Branch> branches;

    @InnerElement
    private DefaultBranch defaultBranch;

    @InnerElement
    private Transition transition;

    @Override
    public void validate(Node node) {
        if (when == null && callbackTriggers == null && keys == null)
            throw new TranscriptionParsingException("Branch is unreachable, missing 'when', 'keys' or 'callbackTriggers' attributes", node);

        if (transition != null && (defaultBranch != null || (branches != null && !branches.isEmpty())))
            throw new TranscriptionParsingException("Branch cannot contain other branches if a transition is defined", node);
    }
}
