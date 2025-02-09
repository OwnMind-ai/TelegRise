package org.telegrise.telegrise.core.elements;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegrise.telegrise.core.elements.actions.ActionElement;
import org.telegrise.telegrise.core.elements.base.NodeElement;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.core.parser.InnerElement;
import org.telegrise.telegrise.core.parser.TranscriptionMemory;
import org.telegrise.telegrise.core.transition.ExecutionOptions;
import org.telegrise.telegrise.exceptions.TranscriptionParsingException;
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

    private static final String INTERRUPTION = "interruption";  //TODO. See TranscriptionManager#transit

    @Attribute(name = "direction", nullable = false)
    private String direction;

    @Attribute(name = "target")
    private GeneratedValue<String> target;

    @Attribute(name = "execute")
    private Boolean execute;

    @Attribute(name = "edit")
    private String edit;

    @Attribute(name = "editSource")
    private String editSource;

    @Attribute(name = "ignoreError")
    private boolean ignoreError = true;

    @InnerElement
    private List<ActionElement> actions;

    @InnerElement
    private Transition nextTransition;

    public Transition(String direction, String target, boolean execute, String edit, String editSource){
        this.direction = direction;
        this.target = GeneratedValue.ofValue(target);
        this.execute = execute;
        this.edit = edit;
        this.editSource = editSource;
    }

    @Override
    public void validate(TranscriptionMemory memory) {
        if (!List.of(BACK, CALLER, JUMP).contains(direction))
            throw new TranscriptionParsingException("Invalid direction '" + this.direction + "', possible directions are: '" +
                    BACK + "', '" + CALLER + "' or '" + JUMP + "'" , node);

        if (!JUMP.equals(this.direction) && (this.nextTransition != null || this.actions != null))
            throw new TranscriptionParsingException("Transitions with direction other then '" + JUMP + "' cannot contain next transition or actions", node);

        if (target == null && JUMP.equals(this.direction))
            throw new TranscriptionParsingException("Target for direction '" + this.direction + "' is not specified" , node);

        if (execute != null && execute && edit != null) 
            throw new TranscriptionParsingException("Attribute 'execute' conflicts with 'edit'", node);

        if(edit == null && editSource != null)
            throw new TranscriptionParsingException("'edit' must be specified with 'editSource'", node);
        
        if(edit != null && !ExecutionOptions.EDIT_FIRST.equals(edit) && (!memory.containsKey(parentTree, edit) || !(memory.get(parentTree, edit) instanceof ActionElement a) || !a.getName().equals(edit)))
            throw new TranscriptionParsingException("Unable to edit on-transition element named '" + edit + "'", node);

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

    public ExecutionOptions getExecutionOptions() {
        return new ExecutionOptions(this.execute, this.edit, this.editSource, ignoreError);
    }
}
