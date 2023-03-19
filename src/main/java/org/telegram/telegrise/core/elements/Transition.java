package org.telegram.telegrise.core.elements;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.ElementField;
import org.telegram.telegrise.core.parser.TranscriptionParsingException;
import org.w3c.dom.Node;

@Element(name = "transition")
@Data @NoArgsConstructor
public class Transition implements TranscriptionElement{
    public static final String NEXT = "next";
    public static final String PREVIOUS = "previous";

    @ElementField(name = "direction", nullable = false)
    private String direction;

    @ElementField(name = "menu")
    private String menu;

    @Override
    public void validate(Node node) {
        if (direction != null && !direction.equals(NEXT) && !direction.equals(PREVIOUS))
            throw new TranscriptionParsingException("Invalid direction '" + this.direction + "', possible directions are: '" + NEXT + "' or '" + PREVIOUS + "'" , node);

        if (!((menu != null && NEXT.equals(direction)) || PREVIOUS.equals(direction)))
            throw new TranscriptionParsingException("Invalid transition syntax", node);
    }
}
