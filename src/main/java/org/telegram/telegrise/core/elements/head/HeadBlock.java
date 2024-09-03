package org.telegram.telegrise.core.elements.head;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.telegram.telegrise.core.elements.NodeElement;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.InnerElement;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Element(name = "head")
@Data
@NoArgsConstructor @AllArgsConstructor
public class HeadBlock extends NodeElement {
    @InnerElement(priority = 1)
    List<Link> links;
}
