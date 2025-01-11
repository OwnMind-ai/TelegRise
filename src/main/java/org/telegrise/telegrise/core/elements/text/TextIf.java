package org.telegrise.telegrise.core.elements.text;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegrise.telegrise.core.ExpressionFactory;
import org.telegrise.telegrise.core.GeneratedValue;
import org.telegrise.telegrise.core.LocalNamespace;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.NodeElement;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.core.parser.TranscriptionMemory;
import org.telegrise.telegrise.core.utils.XMLUtils;
import org.telegrise.telegrise.exceptions.TranscriptionParsingException;
import org.w3c.dom.Node;

@Element(name = "if", checkInner = false)
@Getter @Setter @NoArgsConstructor
public class TextIf extends NodeElement implements TextConditionalElement {
    @Attribute(name = "condition", nullable = false)
    private GeneratedValue<Boolean> condition;

    @Attribute(name = "textblock", priority = 1)
    private boolean textblock;

    private GeneratedValue<String> text;

    @Override
    public void validate(TranscriptionMemory memory) {
        if (text == null)
            throw new TranscriptionParsingException("Text is empty", node);
    }

    @Attribute(name = "", nullable = false)
    private void parseText(Node node, LocalNamespace namespace){
        String s;
        if (this.textblock)
            s = XMLUtils.innerXMLTextBlock(node);
        else
            s = XMLUtils.innerXML(node);

        if (s != null)
            this.text = ExpressionFactory.createExpression(s, String.class, node, namespace);
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
