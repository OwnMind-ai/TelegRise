package org.telegram.telegrise.core.elements;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrise.core.ExpressionFactory;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.LocalNamespace;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.ElementField;
import org.telegram.telegrise.core.parser.EmbeddableElement;
import org.telegram.telegrise.core.utils.XMLUtils;
import org.w3c.dom.Node;

@Element(name = "text")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Text implements TranscriptionElement, EmbeddableElement {
    private GeneratedValue<String> text;

    @ElementField(name = "parseMode", expression = true)
    private GeneratedValue<String> parseMode = GeneratedValue.ofValue("html");

    //TODO message entities list

    public Text(String text, String parseMode){
        this.text = GeneratedValue.ofValue(text);
        this.parseMode = GeneratedValue.ofValue(parseMode);
    }

    @ElementField(nullable = false)
    private void parseText(Node node, LocalNamespace namespace){
        this.text = ExpressionFactory.createExpression(XMLUtils.innerXML(node), String.class, node, namespace);
    }
}
