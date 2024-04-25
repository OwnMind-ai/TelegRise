package org.telegram.telegrise.core.elements.text;

import lombok.*;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegram.telegrise.core.ExpressionFactory;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.LocalNamespace;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.InteractiveElement;
import org.telegram.telegrise.core.elements.StorableElement;
import org.telegram.telegrise.core.elements.TranscriptionElement;
import org.telegram.telegrise.core.parser.*;
import org.telegram.telegrise.core.utils.XMLUtils;
import org.telegram.telegrise.exceptions.TranscriptionParsingException;
import org.telegram.telegrise.types.TextBlock;
import org.w3c.dom.Node;

import java.util.List;
import java.util.function.Function;

@Element(name = "text")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Text implements TranscriptionElement, EmbeddableElement, StorableElement, InteractiveElement<TextBlock> {
    @Getter(value = AccessLevel.NONE)
    private GeneratedValue<String> text;

    @Attribute(name = "name")
    private String name;

    @Attribute(name = "byName")
    private String byName;

    @Attribute(name = "parseMode")
    private GeneratedValue<String> parseMode = GeneratedValue.ofValue("html");

    @Attribute(name = "entities")
    private GeneratedValue<List<MessageEntity>> entities;

    @Attribute(name = "transformer")
    private GeneratedValue<String> transformer;

    @Attribute(name = "textblock", priority = 1)
    private boolean textblock;

    @Attribute(name = "conditional", priority = 1)
    private boolean conditional;

    @InnerElement
    private List<TextConditionalElement> textConditionalElements;

    @Attribute(name = "lang")
    private String lang;

    @Override
    public void validate(Node node, TranscriptionMemory memory) {
        if (text == null && !conditional)
            throw new TranscriptionParsingException("Text is empty", node);

        if (conditional && (textConditionalElements == null || textConditionalElements.isEmpty()))
            throw new TranscriptionParsingException("Conditional text has no conditional elements such as <if> or <else>", node);
    }

    @Override
    public void load(TranscriptionMemory memory) {
        if (this.byName == null) return;

        Text original = memory.get(byName, Text.class, List.of("text"));
        this.text = original.text;
        this.parseMode = original.parseMode;
        this.entities = original.entities;
        this.conditional = original.conditional;
        this.textConditionalElements = original.textConditionalElements;
        this.byName = null;
    }

    public Text(String text, String parseMode){
        this.text = GeneratedValue.ofValue(text);
        this.parseMode = parseMode != null ? GeneratedValue.ofValue(parseMode) : null;
    }

    public String generateText(ResourcePool pool){
        String result = generateString(pool);

        if (this.transformer != null){
            pool.addComponent(result);
            result = transformer.generate(pool);
        }

        return result;
    }

    private String generateString(ResourcePool pool) {
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
        if (!this.conditional && this.byName == null) {
            String raw;
            if (this.textblock) raw = XMLUtils.innerXMLTextBlock(node);
            else raw = XMLUtils.innerXML(node);

            if (raw == null) return;
            this.text = ExpressionFactory.createExpression(raw, String.class, node, namespace);
        }
    }

    @Override
    public void parse(Node parent, LocalNamespace namespace) {
        this.parseText(parent, namespace);
    }

    @Override
    public void store(TranscriptionMemory memory) {
        if (name != null)
            memory.put(name, this);
    }

    @Override
    public TextBlock createInteractiveObject(Function<Update, ResourcePool> resourcePoolFunction) {
        return new TextBlock(this, resourcePoolFunction);
    }
}
