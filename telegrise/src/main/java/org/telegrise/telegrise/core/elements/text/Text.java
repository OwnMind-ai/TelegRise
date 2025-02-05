package org.telegrise.telegrise.core.elements.text;

import lombok.*;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegrise.telegrise.Expression;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.Tree;
import org.telegrise.telegrise.core.elements.base.InteractiveElement;
import org.telegrise.telegrise.core.elements.base.NamedElement;
import org.telegrise.telegrise.core.elements.base.NodeElement;
import org.telegrise.telegrise.core.expressions.ExpressionFactory;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.*;
import org.telegrise.telegrise.core.utils.XMLUtils;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegrise.telegrise.exceptions.TranscriptionParsingException;
import org.telegrise.telegrise.types.TextBlock;
import org.w3c.dom.Node;

import java.util.List;
import java.util.function.Function;

@Element(name = "text", checkInner = false, finishAfterParsing = true)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Text extends NodeElement implements EmbeddableElement, InteractiveElement<TextBlock>, NamedElement,
        org.telegrise.telegrise.transcription.Text {
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

    @Attribute(name = "global")
    private boolean global;

    @InnerElement
    private List<TextConditionalElement> textConditionalElements;

    @Attribute(name = "lang")
    private String lang;

    private Tree parentTree;

    @Override
    public void validate(TranscriptionMemory memory) {
        if (text == null && !conditional && byName == null)
            throw new TranscriptionParsingException("Text is empty", node);

        if (conditional && (textConditionalElements == null || textConditionalElements.isEmpty()))
            throw new TranscriptionParsingException("Conditional text has no conditional elements such as <if> or <else>", node);
    }

    @Override
    public void load(TranscriptionMemory memory) {
        if (this.byName == null) return;

        try {
            Text original = memory.get(parentTree, byName, Text.class, List.of("text"));
            this.text = original.text;
            this.parseMode = original.parseMode;
            this.entities = original.entities;
            this.conditional = original.conditional;
            this.textConditionalElements = original.textConditionalElements;
            this.byName = null;
        } catch (TelegRiseRuntimeException e) {
            throw new TranscriptionParsingException(e.getMessage(), node);
        }
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

        throw new TelegRiseRuntimeException("No conditions has been satisfied in conditional text element", node);
    }

    public GeneratedValue<String> getParseMode(){
        return this.entities == null ? this.parseMode : null;
    }

    @Attribute(name = "", nullable = false)
    private void parseText(Node node, LocalNamespace namespace){
        if (!this.conditional && this.byName == null) {
            String raw;
            if (this.textblock) raw = XMLUtils.innerXMLTextBlock(node);
            else raw = XMLUtils.innerXML(node);

            if (raw == null) return;

            Class<?> controllerClass = namespace.getHandlerClass();
            if (parent instanceof Texts texts && texts.getContext() != null) {
                try {
                    Class<?> contextClass = namespace.getApplicationNamespace().getClass(texts.getContext());
                    if (controllerClass != null && !controllerClass.getName().equals(contextClass.getName()))
                        throw new TranscriptionParsingException("Conflict of contexts between '%s' and '%s'".formatted(contextClass.getName(), controllerClass.getName()), node);

                    controllerClass = contextClass;
                } catch (TelegRiseRuntimeException e){
                    throw new TranscriptionParsingException(e.getMessage(), node);
                }
            }

            this.text = ExpressionFactory.createExpression(raw, String.class, node, new LocalNamespace(controllerClass, namespace.getApplicationNamespace()));
        }
    }

    @Override
    public void parse(Node parent, LocalNamespace namespace) {
        this.parseText(parent, namespace);
    }

    @Override
    public void store(TranscriptionMemory memory) {
        if (name != null)
            memory.put(parentTree, name, this);
    }

    @Override
    public TextBlock createInteractiveObject(Function<Update, ResourcePool> resourcePoolFunction) {
        return new TextBlock(this, resourcePoolFunction);
    }

    @Override
    public Expression<String> getTextExpression() {
        return new Expression<>(this::generateText);
    }
}
