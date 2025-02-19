package org.telegrise.telegrise.core.elements.keyboard;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.base.NodeElement;
import org.telegrise.telegrise.core.expressions.ExpressionFactory;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.core.parser.LocalNamespace;
import org.telegrise.telegrise.core.utils.XMLUtils;
import org.telegrise.telegrise.keyboard.KeyboardState;
import org.w3c.dom.Node;

/**
 * Represents a button of a reply or inline keyboard markup.
 * If the button is for an inline keyboard, the button must have {@code data} or {@code url} attribute.
 *
 * @since 0.1
 * @see <a href="https://core.telegram.org/bots/api#inlinekeyboardmarkup">Telegram API: KeyboardButton</a>
 * @see <a href="https://core.telegram.org/bots/api#inlinekeyboardbutton">Telegram API: InlineKeyboardButton</a>
 */
@Element(name = "button", checkInner = false)
@Getter @Setter
@NoArgsConstructor
public class Button extends NodeElement {
    private GeneratedValue<String> text;

    /**
     * Data to be sent in a callback query to the bot when the button is pressed
     */
    @Attribute(name = "data")
    private GeneratedValue<String> callbackData;

    /**
     * HTTP or tg:// URL to be opened when the button is pressed
     */
    @Attribute(name = "url")
    private GeneratedValue<String> url;

    /**
     * If the specified expression returns false, the row will not be included in the keyboard when sending a message
     */
    @Attribute(name = "when")
    private GeneratedValue<Boolean> when;

    /**
     * If the specified access level is lower than the user's level,
     * the row will not be included in the keyboard when sending a message
     */
    @Attribute(name = "accessLevel")
    private Integer accessLevel;

    @Attribute(name = "", nullable = false)
    private void parseText(Node node, LocalNamespace namespace){
        this.text = ExpressionFactory.createExpression(XMLUtils.innerXML(node), String.class, node, namespace);
    }

    public Button(String text, String callbackData){
        this.text = GeneratedValue.ofValue(text);
        this.callbackData = GeneratedValue.ofValue(callbackData);
    }

    public InlineKeyboardButton createInlineButton(ResourcePool pool, KeyboardState keyboardState){
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
