package org.telegram.telegrise.core.elements;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.elements.actions.ActionElement;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.ElementField;
import org.telegram.telegrise.core.parser.InnerElement;
import org.telegram.telegrise.core.parser.TranscriptionParsingException;
import org.w3c.dom.Node;

import java.util.List;

@Element(name = "branch")
@Data
@NoArgsConstructor
public class Branch implements TranscriptionElement{
    @ElementField(name = "when", expression = true)
    private GeneratedValue<Boolean> when;

    @ElementField(name = "keys")
    private String[] keys;
    @ElementField(name = "callbackTriggers")
    private String[] callbackTriggers;

    @ElementField(name = "invoke", expression = true)
    private GeneratedValue<Void> toInvoke;

    @InnerElement
    private List<ActionElement> actions;

    @InnerElement
    private List<Branch> branches;

    @InnerElement
    private DefaultBranch defaultBranch;

    @Override
    public void validate(Node node) {
        if (when == null && callbackTriggers == null && keys == null)
            throw new TranscriptionParsingException("Branch is unreachable, missing 'when', 'keys' or 'callbackTriggers' attributes", node);
    }
}
