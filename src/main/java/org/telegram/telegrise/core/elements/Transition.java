package org.telegram.telegrise.core.elements;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
public class Transition extends NodeElement {
    public static final String JUMP = "jump";
    public static final String BACK = "back";
    public static final String CALLER = "caller";

    @Attribute(name = "direction", nullable = false)
    private String direction;

    @Attribute(name = "target")
    private GeneratedValue<String> target;

    @Attribute(name = "execute")
    private boolean execute = false;

    @InnerElement
    private List<ActionElement> actions;

    @InnerElement
    private Transition nextTransition;

    @Override
    public void validate(TranscriptionMemory memory) {
        if (!List.of(BACK, CALLER, JUMP).contains(direction))
            throw new TranscriptionParsingException("Invalid direction '" + this.direction + "', possible directions are: '" +
                    BACK + "', '" + CALLER + "' or '" + JUMP + "'" , node);

        if (!JUMP.equals(this.direction) && (this.nextTransition != null || this.actions != null))
            throw new TranscriptionParsingException("Transitions with direction other then '" + JUMP + "' cannot contain next transition or actions", node);

        if (target == null && JUMP.equals(this.direction))
            throw new TranscriptionParsingException("Target for direction '" + this.direction + "' is not specified" , node);

        validateTarget(node, memory);
    }

    private void validateTarget(Node node, TranscriptionMemory memory) {
        if (CALLER.equals(this.direction)) return;

        // Code fragment 'this.target.generate(null)' is allowed in the code bellow because if tests fail,
        // then this.target is for sure an instance of StaticValue class

        if (BACK.equals(direction) && target != null && !this.target.validate(createValidationFor(memory, Branch.class, Tree.class, Root.class)))
            throw new TranscriptionParsingException("Unable to find element named '" + this.target.generate(null) + "'" , node);
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
