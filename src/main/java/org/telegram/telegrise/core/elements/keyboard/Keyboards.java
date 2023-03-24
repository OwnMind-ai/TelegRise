package org.telegram.telegrise.core.elements.keyboard;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrise.core.elements.LinkableElement;
import org.telegram.telegrise.core.elements.TranscriptionElement;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.InnerElement;
import org.telegram.telegrise.core.parser.TranscriptionParsingException;
import org.w3c.dom.Node;

import java.util.List;

@Element(name="keyboards")
@Data @NoArgsConstructor
public class Keyboards implements TranscriptionElement, LinkableElement {
    @InnerElement(nullable = false)
    private List<Keyboard> keyboards;

    @Override
    public void validate(Node node) {
        if(keyboards.stream().noneMatch(k -> k.getName() != null))
            throw new TranscriptionParsingException("Child elements must have a name in order to be linked", node);
    }
}
