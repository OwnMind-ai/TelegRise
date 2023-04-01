package org.telegram.telegrise.core.elements.actions;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.elements.Text;
import org.telegram.telegrise.core.elements.TranscriptionElement;
import org.telegram.telegrise.core.parser.*;
import org.w3c.dom.Node;

@Element(name = "frame")
@Data @NoArgsConstructor
public class Frame implements TranscriptionElement {
    public static final String SEND = "send";
    public static final String EDIT = "edit";

    @Attribute(name = "delay")
    private GeneratedValue<Float> delay = GeneratedValue.ofValue(0f);

    @Attribute(name = "action")
    private String action = EDIT;

    @InnerElement(nullable = false)
    private Text text;

    @Override
    public void validate(Node node, TranscriptionMemory memory) {
        if (!action.equals(EDIT) && !action.equals(SEND))
            throw new TranscriptionParsingException("'action' attribute must either '" + SEND + "' or '" + EDIT + "'", node);
    }
}
