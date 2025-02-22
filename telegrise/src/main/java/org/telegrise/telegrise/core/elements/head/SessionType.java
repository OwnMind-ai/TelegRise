package org.telegrise.telegrise.core.elements.head;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegrise.telegrise.core.elements.base.NodeElement;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.core.parser.LocalNamespace;
import org.telegrise.telegrise.core.utils.XMLUtils;
import org.w3c.dom.Node;

/**
 * Defines a session type of this bot.
 * <pre>
 * {@code
 * <sessionType>chat</sessionType>
 * <sessionType>user</sessionType>
 * }
 * </pre>
 *
 * @since 0.9
 */
@Element(name = "sessionType")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SessionType extends NodeElement {
    private String type;

    @Attribute(name = "", nullable = false)
    private void parseToken(Node node, LocalNamespace namespace){
        this.type = XMLUtils.innerXML(node);
    }
}
