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
 * Defines a username of this bot. This value will be used for handling commands in group chats.
 * <pre>
 * {@code
 * <username>your bot username</username>
 * }
 * </pre>
 *
 * @since 0.9
 */
@Element(name = "username")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Username extends NodeElement {
    private GeneratedValue<String> username;

    @Attribute(name = "", nullable = false)
    private void parseToken(Node node, LocalNamespace namespace){
        var raw = XMLUtils.innerXML(node);
        this.username = ExpressionFactory.createExpression(raw, String.class, node, namespace);
    }
}
