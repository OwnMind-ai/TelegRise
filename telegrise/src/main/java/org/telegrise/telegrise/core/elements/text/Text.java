package org.telegrise.telegrise.core.elements.text;

import lombok.*;
import org.jetbrains.annotations.ApiStatus;
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
import java.util.stream.IntStream;

/**
 * An element that is used to define a text with optional formating for message-related API methods.
 * <p>
 * By default, this element uses HTML parse mode to format its content.
 * The text can contain Java expressions and method references.
 * If {@code textblock} is true, the element will use striped whitespaces in linebreaks to parse text,
 * otherwise tag {@code <br/>} must be used to create a line break.
 * <pre>
 * {@code
 * <text textblock="true">
 *     Hello, ${#getFirstName}!
 *
 *     <i>How are you?</i>
 * </text>
 * }
 * </pre>
 * <p>
 * Text element can have multiple variants under certain conditions.
 * In this case, defining {@code textblock}, {@code parseMode}
 * and text in the {@code <text>} is obsolete and all the configuration must be moved to individual parts themselves.
 * Conditional text must have an <b>else</b> case.
 * <pre>
 * {@code
 * <text textblock="true">
 *     <if condition="#condition">Hello!</if>
 *     <else>Bye!</else>
 * </text>
 * }
 * </pre>
 *
 * @see TextElse
 * @see TextIf
 * @since 0.1
 */
@Element(name = "text", checkInner = false, finishAfterParsing = true)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Text extends NodeElement implements EmbeddableElement, InteractiveElement<TextBlock>, NamedElement,
        org.telegrise.telegrise.transcription.Text {
    @Getter(value = AccessLevel.NONE)
    private GeneratedValue<String> text;

    /**
     * Name of the text to be used to duplicate text
     */
    @Attribute(name = "name", priority = 2)
    private String name;

    /**
     * Use to duplicate text by name
     */
    @Attribute(name = "byName", priority = 3)
    private String byName;

    /**
     * Parse mode for text formating, default 'html'
     */
    @Attribute(name = "parseMode")
    private GeneratedValue<String> parseMode = GeneratedValue.ofValue("html");

    /**
     * List of formating entities
     */
    @Attribute(name = "entities")
    private GeneratedValue<List<MessageEntity>> entities;

    /**
     * An expression that can be used to transform text after being generated
     */
    @Attribute(name = "transformer")
    private GeneratedValue<String> transformer;

    /**
     * If set to true, the element will use striped whitespaces in linebreaks to parse text,
     * otherwise tag {@code <br/>} must be used to create a line break.
     */
    @Attribute(name = "textblock", priority = 1)
    private boolean textblock;

    /**
     * If true, the text will be available outside a parent tree
     */
    @Attribute(name = "global")
    private boolean global;

    @InnerElement
    private List<TextConditionalElement> textConditionalElements;

    @ApiStatus.Experimental  //TODO dont forget to mention in docs here and Texts when implemented
    @Attribute(name = "lang")
    private String lang;

    private Tree parentTree;
    private boolean conditional;

    @Override
    public void validate(TranscriptionMemory memory) {
        if (text == null && !conditional && byName == null)
            throw new TranscriptionParsingException("Text is empty", node);

        if (textConditionalElements != null && textConditionalElements.stream().noneMatch(TextElse.class::isInstance))
            throw new TranscriptionParsingException("Conditional text must have an else case", node);
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
        this.conditional = this.determineIfConditional(node);
        if (this.conditional || this.byName != null) return;

        String raw;
        if (this.textblock) raw = XMLUtils.innerXMLTextBlock(node);
        else raw = XMLUtils.innerXML(node);

        if (raw == null) return;
        this.text = ExpressionFactory.createExpression(raw, String.class, node, namespace);
    }

    private boolean determineIfConditional(Node node) {
        return IntStream.range(0, node.getChildNodes().getLength())
                .mapToObj(node.getChildNodes()::item)
                .anyMatch(n -> n.getNodeType() == Node.ELEMENT_NODE && (n.getNodeName().equals("if") || n.getNodeName().equals("else")));
    }

    @Override
    public void parse(Node parent, LocalNamespace namespace) {
        this.parseText(parent, namespace);
        if (isConditional())
            throw new TranscriptionParsingException("Text without <text> element cannot be conditional. Please, wrap your text in <text> tag.", parent);
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
