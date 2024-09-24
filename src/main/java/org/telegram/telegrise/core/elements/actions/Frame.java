package org.telegram.telegrise.core.elements.actions;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.elements.NodeElement;
import org.telegram.telegrise.core.elements.text.Text;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.InnerElement;
import org.telegram.telegrise.core.parser.TranscriptionMemory;
import org.telegram.telegrise.exceptions.TranscriptionParsingException;

@Element(name = "frame")
@Getter @Setter @NoArgsConstructor
public class Frame extends NodeElement {
    public static final String SEND = "send";
    public static final String EDIT = "edit";

    @Attribute(name = "delay")
    private GeneratedValue<Float> delay = GeneratedValue.ofValue(0f);

    @Attribute(name = "action")
    private String action = EDIT;

    @InnerElement(nullable = false)
    private Text text;

    @Override
    public void validate(TranscriptionMemory memory) {
        if (!action.equals(EDIT) && !action.equals(SEND))
            throw new TranscriptionParsingException("'action' attribute must either '" + SEND + "' or '" + EDIT + "'", node);
    }
}
