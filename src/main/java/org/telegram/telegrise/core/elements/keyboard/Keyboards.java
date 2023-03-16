package org.telegram.telegrise.core.elements.keyboard;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrise.core.elements.StorableElement;
import org.telegram.telegrise.core.elements.TranscriptionElement;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.InnerElement;
import org.telegram.telegrise.core.parser.ParserMemory;

import java.util.List;

@Element(name="keyboards")
@Data @NoArgsConstructor
public class Keyboards implements StorableElement, TranscriptionElement {
    @InnerElement(nullable = false)
    private List<Keyboard> keyboards;

    @Override
    public void store(ParserMemory memory) {
        keyboards.forEach(k -> k.store(memory));
    }
}
