package org.telegram.telegrise.core.elements;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.elements.actions.ActionElement;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.InnerElement;
import org.telegram.telegrise.core.parser.TranscriptionMemory;
import org.telegram.telegrise.exceptions.TranscriptionParsingException;
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

@Element(name = "transition", finishAfterParsing = true)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Transition extends NodeElement {
    public static final String PREVIOUS = "previous";
    public static final String JUMP = "jump";
    public static final String LOCAL = "local";
    public static final String CALLER = "caller";
    public static final String MENU_TYPE = "menu";
    private static final String TREE_TYPE = "tree";
    public static final List<String> TYPE_LIST = List.of(MENU_TYPE, TREE_TYPE);

    @Attribute(name = "direction", nullable = false)
    private String direction;

    @Attribute(name = "target")
    private GeneratedValue<String> target;

    @Attribute(name = "type")
    private String type;

    @Attribute(name = "execute")
    private boolean execute = true;

    @InnerElement
    private List<ActionElement> actions;

    @InnerElement
    private Transition nextTransition;

    @Override
    public void validate(TranscriptionMemory memory) {
        if (direction != null && !direction.equals(PREVIOUS) && !direction.equals(JUMP)
                && !direction.equals(LOCAL) && !direction.equals(CALLER))
            throw new TranscriptionParsingException("Invalid direction '" + this.direction + "', possible directions are: '" +
                    PREVIOUS + "', '" + LOCAL + "' or '" + JUMP + "'" , node);

        if (!JUMP.equals(this.direction) && (this.nextTransition != null || this.actions != null))
            throw new TranscriptionParsingException("Transitions with direction other then '" + JUMP + "' cannot contain next transition or actions", node);

        if (type != null && !TYPE_LIST.contains(this.type))
            throw new TranscriptionParsingException("Invalid type '" + type + "', possible types are: " + String.join(", ", TYPE_LIST), node);

        if (target == null && !CALLER.equals(this.direction))
            throw new TranscriptionParsingException("Target for direction '" + this.direction + "' is not specified" , node);

        if (type == null && CALLER.equals(this.direction))
            throw new TranscriptionParsingException("Caller type for direction '" + CALLER + "' is not specified", node);

        validateTarget(node, memory);
    }

    private void validateTarget(Node node, TranscriptionMemory memory) {
        if (CALLER.equals(this.direction)) return;

        // Code fragment 'this.target.generate(null)' is allowed in the code bellow because if tests fail,
        // then this.target is for sure an instance of StaticValue class

        if (LOCAL.equals(direction) && !this.target.validate(createValidationFor(memory, Branch.class)))
            throw new TranscriptionParsingException("Unable to find branch named '" + this.target.generate(null) + "'" , node);

        if ((JUMP.equals(direction) || PREVIOUS.equals(direction)) && !this.target.validate(createValidationFor(memory, Tree.class, Root.class)))
            throw new TranscriptionParsingException("Unable to find element named '" + this.target.generate(null) + "'", node);
    }

    @SafeVarargs
    private Predicate<String> createValidationFor(TranscriptionMemory memory, Class<? extends NodeElement>... classes) {
        return s -> {
            if (s == null) return false;
            NodeElement element = memory.get(parentTree, s);

            return Arrays.stream(classes).anyMatch(c -> c.isInstance(element));
        };
    }
}
