package org.telegram.telegrise.core.elements.text;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrise.core.ExpressionFactory;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.LocalNamespace;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.utils.XMLUtils;
import org.w3c.dom.Node;

@Element(name = "if")
@Data @NoArgsConstructor
public class TextIf implements TextConditionalElement {
    @Attribute(name = "condition", nullable = false)
    private GeneratedValue<Boolean> condition;

    private GeneratedValue<String> text;

    @Attribute(nullable = false)
    private void parseText(Node node, LocalNamespace namespace){
        this.text = ExpressionFactory.createExpression(XMLUtils.innerXML(node), String.class, node, namespace);
    }

    @Override
    public boolean isApplicable(ResourcePool pool) {
        return condition.generate(pool);
    }

    @Override
    public String getString(ResourcePool pool) {
        return text.generate(pool);
    }
}
