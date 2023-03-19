package org.telegram.telegrise.core.elements.keyboard;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrise.core.ExpressionFactory;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.LocalNamespace;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.TranscriptionElement;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.ElementField;
import org.telegram.telegrise.core.utils.XMLUtils;
import org.w3c.dom.Node;

@Element(name = "button")
@Data
@NoArgsConstructor
public class Button implements TranscriptionElement {
    private GeneratedValue<String> text;

    @ElementField(name = "callbackData", expression = true)
    private GeneratedValue<String> callbackData;

    @ElementField(name = "url", expression = true)
    private GeneratedValue<String> url;

    @ElementField(name = "when", expression = true)
    private GeneratedValue<Boolean> when = GeneratedValue.ofValue(true);

    @ElementField(nullable = false)
    private void parseText(Node node, LocalNamespace namespace){
        this.text = ExpressionFactory.createExpression(XMLUtils.innerXML(node), String.class, node, namespace);
    }

    public Button(String text, String callbackData){
        this.text = GeneratedValue.ofValue(text);
        this.callbackData = GeneratedValue.ofValue(callbackData);
    }

    public InlineKeyboardButton createInlineButton(ResourcePool pool){
        return InlineKeyboardButton.builder()
                .text(text.generate(pool))
                .url(generateNullableProperty(url, pool))
                .callbackData(generateNullableProperty(callbackData, pool))
                .build();
    }

    public KeyboardButton createKeyboardButton(ResourcePool pool){
        return KeyboardButton.builder().text(text.generate(pool)).build();
    }
}
