package org.telegram.telegrise.core.elements;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrise.core.ExpressionFactory;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.LocalNamespace;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.ElementField;
import org.telegram.telegrise.core.parser.EmbeddableElement;
import org.telegram.telegrise.core.utils.XMLUtils;
import org.w3c.dom.Node;

import java.util.List;

@Element(name = "text")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Text implements TranscriptionElement, EmbeddableElement {
    private GeneratedValue<String> text;

    @ElementField(name = "parseMode", expression = true)
    private GeneratedValue<String> parseMode;

    @ElementField(name = "entities", expression = true)
    private GeneratedValue<List<MessageEntity>> entities;

    public Text(String text, String parseMode){
        this.text = GeneratedValue.ofValue(text);
        this.parseMode = parseMode != null ? GeneratedValue.ofValue(parseMode) : null;
    }

    @ElementField(nullable = false)
    private void parseText(Node node, LocalNamespace namespace){
        this.text = ExpressionFactory.createExpression(XMLUtils.innerXML(node), String.class, node, namespace);
    }

    @Override
    public void parse(Node parent, LocalNamespace namespace) {
        this.text = ExpressionFactory.createExpression(XMLUtils.innerXML(parent), String.class, parent, namespace);
    }
}
