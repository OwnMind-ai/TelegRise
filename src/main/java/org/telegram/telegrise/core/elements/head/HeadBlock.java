package org.telegram.telegrise.core.elements.head;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrise.core.elements.TranscriptionElement;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.InnerElement;

import java.util.List;

@Element(name = "head")  //FIXME find a better name
@Data
@NoArgsConstructor
public class HeadBlock implements TranscriptionElement {
    @InnerElement(priority = 1)
    List<Link> links;
}
