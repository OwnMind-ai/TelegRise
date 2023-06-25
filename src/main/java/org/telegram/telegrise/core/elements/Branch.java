package org.telegram.telegrise.core.elements;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.elements.actions.ActionElement;
import org.telegram.telegrise.core.parser.*;
import org.w3c.dom.Node;

import java.util.List;

import static org.telegram.telegrise.core.elements.Tree.INTERRUPT_BY_ALL;
import static org.telegram.telegrise.core.elements.Tree.improperInterruptionScopes;

@Element(name = "branch")
@Data
@NoArgsConstructor
public class Branch implements StorableElement, TranscriptionElement{
    @Attribute(name = "name")
    private String name;

    @Attribute(name = "allowedInterruptions")
    private String[] allowedInterruptions = {INTERRUPT_BY_ALL};

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
    public void validate(Node node, TranscriptionMemory memory) {
        if (when == null && callbackTriggers == null && keys == null)
            throw new TranscriptionParsingException("Branch is unreachable, missing 'when', 'keys' or 'callbackTriggers' attributes", node);

        if (transition != null && (defaultBranch != null || (branches != null && !branches.isEmpty())))
            throw new TranscriptionParsingException("Branch cannot contain other branches if a transition is defined", node);

        if (improperInterruptionScopes(this.allowedInterruptions))
            throw new TranscriptionParsingException("Undefined interruption scopes", node);
    }

    @Override
    public void store(TranscriptionMemory memory) {
        if (this.name != null)
            memory.put(this.name, this);
    }
}
