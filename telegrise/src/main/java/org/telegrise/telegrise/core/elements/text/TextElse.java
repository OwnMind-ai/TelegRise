package org.telegrise.telegrise.core.elements.text;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.base.NodeElement;
import org.telegrise.telegrise.core.expressions.ExpressionFactory;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.core.parser.LocalNamespace;
import org.telegrise.telegrise.core.parser.TranscriptionMemory;
import org.telegrise.telegrise.core.utils.XMLUtils;
import org.telegrise.telegrise.exceptions.TranscriptionParsingException;
import org.w3c.dom.Node;

/**
 * A conditional branch of {@code <text>} element.
 * If all other conditions failed before this one, the text of this element will be used.
 *
 * @since 0.1
 * @see Text
 */
@Element(name = "else", checkInner = false)
@Getter @Setter @NoArgsConstructor
public class TextElse extends NodeElement implements TextConditionalElement {
    /**
     * If set to true, the element will use striped whitespaces in linebreaks to parse text,
     * otherwise tag {@code <br/>} must be used to create a line break.
     */
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
        return true;
    }

    @Override
    public String getString(ResourcePool pool) {
        return text.generate(pool);
    }
}