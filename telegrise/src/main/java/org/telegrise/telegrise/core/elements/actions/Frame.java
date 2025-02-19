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

/**
 * Represents a text frame of the animations.
 *
 * @since 0.1
 * @see Animate
 */
@Element(name = "frame")
@Getter @Setter @NoArgsConstructor
public class Frame extends NodeElement {
    public static final String SEND = "send";
    public static final String EDIT = "edit";

    /**
     * Delay after the frame in addition to an animation period.
     */
    @Attribute(name = "delay")
    private GeneratedValue<Float> delay = GeneratedValue.ofValue(0f);

    /**
     * Action to execute at the frame: 'send' or 'edit' (by default)
     */
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
