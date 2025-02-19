package org.telegrise.telegrise.core.elements;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.telegrise.telegrise.Expression;
import org.telegrise.telegrise.core.elements.actions.ActionElement;
import org.telegrise.telegrise.core.elements.base.BranchingElement;
import org.telegrise.telegrise.core.elements.base.NodeElement;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.core.parser.InnerElement;
import org.telegrise.telegrise.core.parser.TranscriptionMemory;
import org.telegrise.telegrise.exceptions.TranscriptionParsingException;
import org.telegrise.telegrise.utils.ChatTypes;

import java.util.List;
import java.util.Objects;

import static org.telegrise.telegrise.core.elements.Tree.improperInterruptionScopes;

/**
 * A branch of a tree or another branch that can be chosen to go to if its conditions are met.
 *
 * @see Tree
 * @since 0.1
 */
@Element(name = "branch", finishAfterParsing = true)
@Getter @Setter @NoArgsConstructor
public class Branch extends NodeElement implements org.telegrise.telegrise.transcription.Branch, BranchingElement {
    /**
     * Name of the branch that can be used as a target for transition
     */
    @Attribute(name = "name")
    private String name;

    /**
     * Defines types of interruptions that allowed in this branch and its children
     */
    @Attribute(name = "allowedInterruptions")
    private String[] allowedInterruptions;

    /**
     * Defines a predicate that will be used to determine if this branch can handle an update
     */
    @Attribute(name = "when")
    private GeneratedValue<Boolean> when;

    /**
     * Text of a message (the key) or list of them that this branch will respond to
     */
    @Attribute(name = "key")
    private String[] keys;
    /**
     * Callback data or a list of them that this branch will respond to
     */
    @Attribute(name = "callback")
    private String[] callbackTriggers;

    /**
     * An expression or method reference
     * to execute if this branch is chosen to handle an update before all action elements.
     * Equivalent to:
     * <pre>
     * {@code
     * <branch ...>
     *     <invoke method="..."/>
     *     ...
     * </branch>
     * }
     */
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

    @Override
    public @Nullable Expression<Boolean> getWhenExpression() {
        return when != null ? when.toExpression() : null;
    }

    @Override
    public @Nullable Expression<Void> getToInvokeExpression() {
        return toInvoke != null ? toInvoke.toExpression() : null;
    }
}
