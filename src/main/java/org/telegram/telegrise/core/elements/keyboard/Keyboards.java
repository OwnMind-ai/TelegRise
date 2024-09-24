package org.telegram.telegrise.core.elements.keyboard;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.telegram.telegrise.core.elements.LinkableElement;
import org.telegram.telegrise.core.elements.NodeElement;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.InnerElement;
import org.telegram.telegrise.core.parser.TranscriptionMemory;
import org.telegram.telegrise.exceptions.TranscriptionParsingException;

import java.util.List;

@Element(name="keyboards")
@Getter @Setter @NoArgsConstructor
public class Keyboards extends NodeElement implements LinkableElement {
    @InnerElement(nullable = false)
    private List<Keyboard> keyboards;

    @Override
    public void validate(TranscriptionMemory memory) {
        if(keyboards.stream().noneMatch(k -> k.getName() != null))
            throw new TranscriptionParsingException("Child elements must have a name in order to be linked", node);
    }
}
