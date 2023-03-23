package org.telegram.telegrise.core.elements;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.TranscriptionParsingException;
import org.w3c.dom.Node;

@Element(name = "transition")
@Data @NoArgsConstructor
public class Transition implements TranscriptionElement{
    public static final String NEXT = "next";
    public static final String PREVIOUS = "previous";
    public static final String JUMP = "jump";
    public static final String LOCAL = "local";

    @Attribute(name = "direction", nullable = false)
    private String direction;

    @Attribute(name = "target", nullable = false)
    private String target;

    @Override
    public void validate(Node node) {
        if (direction != null && !direction.equals(NEXT) && !direction.equals(PREVIOUS) && !direction.equals(JUMP) && !direction.equals(LOCAL))
            throw new TranscriptionParsingException("Invalid direction '" + this.direction + "', possible directions are: '"
                    + NEXT + "', '" + PREVIOUS + "', '" + LOCAL + "' or '" + JUMP + "'" , node);
    }
}
