package org.telegrise.telegrise.core.elements.actions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegrise.telegrise.core.elements.base.NodeElement;
import org.telegrise.telegrise.core.elements.text.Text;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.core.parser.InnerElement;
import org.telegrise.telegrise.core.parser.TranscriptionMemory;
import org.telegrise.telegrise.exceptions.TranscriptionParsingException;

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
