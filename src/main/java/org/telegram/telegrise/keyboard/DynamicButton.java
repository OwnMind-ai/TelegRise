package org.telegram.telegrise.keyboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.keyboard.Button;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a dynamic button that used in <a href="#{@link}">{@link org.telegram.telegrise.keyboard.DynamicKeyboard DynamicKeyboard}</a>.
 * Dynamic buttons can possess attributes that store pre-defined expressions within <code>GeneratedValue</code> objects.
 * These buttons can be dynamically disabled or enabled based on specified conditions that can be setted using <code>setWhen</code> method.
 * 
 * @see DynamicKeyboard
 * @see DynamicRow
 * @since 0.4
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class DynamicButton implements Serializable {
    /**
    * Creates a dynamic button from a <a href="#{@link}">{@link org.telegram.telegrise.core.elements.keyboard.Button Button}</a> object.
    * 
    * @param button The Button object to create a dynamic button from.
    * @return The created DynamicButton object.
    */
    public static DynamicButton ofButton(Button button){
        return new DynamicButton(
                button.getText(),
                button.getCallbackData(),
                button.getUrl(),
                Objects.requireNonNullElse(button.getWhen(), GeneratedValue.ofValue(true))
        );
    }

    /**
    * Creates a dynamic button from a <a href="#{@link}">{@link org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton InlineKeyboardButton}</a> object.
    * Enabled by default.
    * 
    * @param button The InlineKeyboardButton object to create a dynamic button from.
    * @return The created DynamicButton object.
    */
    public static DynamicButton of(InlineKeyboardButton button){
        return new DynamicButton(
                GeneratedValue.ofValue(button.getText()),
                GeneratedValue.ofValue(button.getCallbackData()),
                GeneratedValue.ofValue(button.getUrl()),
                GeneratedValue.ofValue(true)
        );
    }

    /**
    * Creates a dynamic button from a <a href="#{@link}">{@link org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton KeyboardButton}</a> object.
    * Enabled by default.
    * 
    * @param button The KeyboardButton object to create a dynamic button from.
    * @return The created DynamicButton object.
    */
    public static DynamicButton of(KeyboardButton button){
        return new DynamicButton(
                GeneratedValue.ofValue(button.getText()),
                null, null,
                GeneratedValue.ofValue(true)
        );
    }

    private GeneratedValue<String> text;
    private GeneratedValue<String> callbackData;
    private GeneratedValue<String> url;
    private GeneratedValue<Boolean> when = GeneratedValue.ofValue(true);

    /**
    * Creates an InlineKeyboardButton object using the dynamic button's properties
    * and a given <code>ResourcePool</code>.
    * 
    * @param pool The ResourcePool object to use for creating the InlineKeyboardButton.
    * @return The created InlineKeyboardButton object.
    * @see org.telegram.telegrise.core.ResourcePool
    */
    public InlineKeyboardButton createInlineButton(ResourcePool pool){
        return InlineKeyboardButton.builder()
                .text(text.generate(pool))
                .url(url == null ? null : url.generate(pool))
                .callbackData(callbackData == null ? null : callbackData.generate(pool))
                .build();
    }

    /**
    * Creates an KeyboardButton object using the dynamic button's properties
    * and a given <code>ResourcePool</code>.
    * 
    * @param pool The ResourcePool object to use for creating the KeyboardButton.
    * @return The created KeyboardButton object.
    * @see org.telegram.telegrise.core.ResourcePool
    */
    public KeyboardButton createKeyboardButton(ResourcePool pool){
        return KeyboardButton.builder().text(text.generate(pool)).build();
    }
}
