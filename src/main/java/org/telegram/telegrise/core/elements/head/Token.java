package org.telegram.telegrise.core.elements.head;

import org.telegram.telegrise.core.ExpressionFactory;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.LocalNamespace;
import org.telegram.telegrise.core.elements.NodeElement;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.utils.XMLUtils;
import org.w3c.dom.Node;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Element(name = "token")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Token extends NodeElement {
    private GeneratedValue<String> token;

    @Attribute(name = "", nullable = false)
    private void parseToken(Node node, LocalNamespace namespace){
        var raw = XMLUtils.innerXML(node);
        this.token = ExpressionFactory.createExpression(raw, String.class, node, namespace);
    }
}
