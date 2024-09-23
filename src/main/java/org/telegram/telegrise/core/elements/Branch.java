package org.telegram.telegrise.core.elements;

import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.telegram.telegrise.ChatTypes;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.elements.actions.ActionElement;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.InnerElement;
import org.telegram.telegrise.core.parser.TranscriptionMemory;
import org.telegram.telegrise.exceptions.TranscriptionParsingException;

import java.util.List;
import java.util.Objects;

import static org.telegram.telegrise.core.elements.Tree.improperInterruptionScopes;

@Element(name = "branch", finishAfterParsing = true)
@Getter @Setter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class Branch extends NodeElement implements BranchingElement {
    @Attribute(name = "name")
    private String name;

    @Attribute(name = "allowedInterruptions")
    private String[] allowedInterruptions;

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

    private int level = -1;

    @Override
    public void validate(TranscriptionMemory memory) {
        if (level < 2)   // Branches can't be at level 1, only trees do
            throw new TranscriptionParsingException("Invalid branch level: %d. \nThis is an internal error. Please, report to https://github.com/OwnMind-ai/TelegRise/issues if this error occurred and attach: error message, the .xml file with problematic branch.".formatted(level), node);

        if (when == null && callbackTriggers == null && keys == null)
            throw new TranscriptionParsingException("Branch is unreachable, missing 'when', 'keys' or 'callbackTriggers' attributes", node);

        if (transition != null && (defaultBranch != null || (branches != null && !branches.isEmpty())))
            throw new TranscriptionParsingException("Branch cannot contain other branches if a transition is defined", node);

        if (this.allowedInterruptions != null && improperInterruptionScopes(this.allowedInterruptions))
            throw new TranscriptionParsingException("Undefined interruption scopes", node);
    }

    @Override
    public String[] getChatTypes() {
        return new String[]{ChatTypes.ALL};
    }

    @Override
    public List<? extends BranchingElement> getChildren() {
        return Objects.requireNonNullElse(branches, List.of());
    }

    @Override
    public void store(TranscriptionMemory memory) {
        if (this.name != null)
            memory.put(parentTree, this.name, this);
    }
}
