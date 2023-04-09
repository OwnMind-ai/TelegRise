package org.telegram.telegrise.core.elements.text;

import lombok.*;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrise.TelegRiseRuntimeException;
import org.telegram.telegrise.core.ExpressionFactory;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.LocalNamespace;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.TranscriptionElement;
import org.telegram.telegrise.core.parser.*;
import org.telegram.telegrise.core.utils.XMLUtils;
import org.w3c.dom.Node;

import java.util.List;

@Element(name = "text")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Text implements TranscriptionElement, EmbeddableElement {
    @Getter(value = AccessLevel.NONE)
    private GeneratedValue<String> text;

    @Attribute(name = "parseMode")
    private GeneratedValue<String> parseMode = GeneratedValue.ofValue("html");

    @Attribute(name = "entities")
    private GeneratedValue<List<MessageEntity>> entities;

    @Attribute(name = "conditional", priority = 1)
    private boolean conditional;

    @InnerElement
    private List<TextConditionalElement> textConditionalElements;

    @Override
    public void validate(Node node, TranscriptionMemory memory) {
        if (conditional && (textConditionalElements == null || textConditionalElements.isEmpty()))
            throw new TranscriptionParsingException("Conditional text has no conditional elements such as <if> or <else>", node);
    }

    public Text(String text, String parseMode){
        this.text = GeneratedValue.ofValue(text);
        this.parseMode = parseMode != null ? GeneratedValue.ofValue(parseMode) : null;
    }

    public String generateText(ResourcePool pool){
        if (!conditional) return text.generate(pool);

        for (TextConditionalElement element : this.textConditionalElements)
            if (element.isApplicable(pool))
                return element.getString(pool);

        throw new TelegRiseRuntimeException("No conditions has been satisfied in conditional text element");
    }

    public GeneratedValue<String> getParseMode(){
        return this.entities == null ? this.parseMode : null;
    }

    @Attribute(nullable = false)
    private void parseText(Node node, LocalNamespace namespace){
        if (!this.conditional)
            this.text = ExpressionFactory.createExpression(XMLUtils.innerXML(node), String.class, node, namespace);
    }

    @Override
    public void parse(Node parent, LocalNamespace namespace) {
        this.parseText(parent, namespace);
    }
}
