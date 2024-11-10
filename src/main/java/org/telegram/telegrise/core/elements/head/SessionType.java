package org.telegram.telegrise.core.elements.head;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrise.core.LocalNamespace;
import org.telegram.telegrise.core.elements.NodeElement;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.utils.XMLUtils;
import org.w3c.dom.Node;

@Element(name = "sessionType")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SessionType extends NodeElement {
    private String type;

    @Attribute(name = "", nullable = false)
    private void parseToken(Node node, LocalNamespace namespace){
        this.type = XMLUtils.innerXML(node);
    }
}
