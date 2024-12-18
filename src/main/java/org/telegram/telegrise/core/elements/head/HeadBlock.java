package org.telegram.telegrise.core.elements.head;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrise.core.elements.NodeElement;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.InnerElement;

import java.util.List;

@Element(name = "head")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class HeadBlock extends NodeElement {
    @InnerElement(priority = 1)
    private List<Link> links;

    @InnerElement
    private Token token;

    @InnerElement
    private Username username;

    @InnerElement
    private SessionType sessionType;
}
