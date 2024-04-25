package org.telegram.telegrise.core.elements;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.elements.actions.ActionElement;
import org.telegram.telegrise.core.parser.*;
import org.telegram.telegrise.exceptions.TranscriptionParsingException;
import org.w3c.dom.Node;

import java.util.List;

@Element(name = "transition")
@Data @NoArgsConstructor @AllArgsConstructor
public class Transition implements TranscriptionElement{
    public static final String NEXT = "next";
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
    public void validate(Node node, TranscriptionMemory memory) {
        if (direction != null && !direction.equals(NEXT) && !direction.equals(PREVIOUS) && !direction.equals(JUMP)
                && !direction.equals(LOCAL) && !direction.equals(CALLER))
            throw new TranscriptionParsingException("Invalid direction '" + this.direction + "', possible directions are: '"
                    + NEXT + "', '" + PREVIOUS + "', '" + LOCAL + "' or '" + JUMP + "'" , node);

        if (!JUMP.equals(this.direction) && (this.nextTransition != null || this.actions != null))
            throw new TranscriptionParsingException("Transitions with direction other then '" + JUMP + "' cannot contain next transition or actions", node);

        if (type != null && !TYPE_LIST.contains(this.type))
            throw new TranscriptionParsingException("Invalid type '" + type + "', possible types are: " + String.join(", ", TYPE_LIST), node);

        if (target == null && !CALLER.equals(this.direction))
            throw new TranscriptionParsingException("Target for direction '" + this.direction + "' is not specified" , node);

        if (type == null && CALLER.equals(this.direction))
            throw new TranscriptionParsingException("Caller type for direction '" + CALLER + "' is not specified", node);
    }
}
