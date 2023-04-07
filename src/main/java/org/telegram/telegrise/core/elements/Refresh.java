package org.telegram.telegrise.core.elements;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrise.core.parser.*;
import org.w3c.dom.Node;

@Element(name = "refresh")
@Data @NoArgsConstructor
public class Refresh implements TranscriptionElement{
    public static final String CALLBACK = "callback";
    public static final String LAST = "last";

    @Attribute(name = "type")
    private String type = LAST;

    @Attribute(name = "keyboardId")
    private String keyboardId;

    @Attribute(name = "sneaky")
    private boolean sneaky;

    @Attribute(name = "transition")
    private boolean transit = true;

    @Attribute(name = "execute")
    private boolean execute;

    @InnerElement
    private Text text;

    @Override
    public void validate(Node node, TranscriptionMemory memory) {
        if (!LAST.equals(type) && !CALLBACK.equals(type))
            throw new TranscriptionParsingException("Invalid refresh type '" + type + "', possible types are: '"
                    + LAST + "' or '" + CALLBACK + "'" , node);

        if (text == null && keyboardId == null)
            throw new TranscriptionParsingException("No keyboard or text specified", node);
    }
}
