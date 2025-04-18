package org.telegrise.telegrise.core.elements.head;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegrise.telegrise.core.elements.base.NodeElement;
import org.telegrise.telegrise.core.expressions.ExpressionFactory;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.core.parser.LocalNamespace;
import org.telegrise.telegrise.core.utils.XMLUtils;
import org.w3c.dom.Node;

/**
 * Defines a token of this bot.
 * <pre>
 * {@code
 * <token>your bot token</token>
 * }
 * </pre>
 *
 * @since 0.9
 */
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
