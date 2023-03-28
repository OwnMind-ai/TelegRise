package org.telegram.telegrise.types;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.keyboard.Button;

import java.io.Serializable;

@Data @NoArgsConstructor @AllArgsConstructor
public class DynamicButton implements Serializable {
    public static DynamicButton ofButton(Button button){
        return new DynamicButton(
                button.getText(),
                button.getCallbackData(),
                button.getUrl(),
                button.getWhen()
        );
    }

    public static DynamicButton of(InlineKeyboardButton button){
        return new DynamicButton(
                GeneratedValue.ofValue(button.getText()),
                GeneratedValue.ofValue(button.getCallbackData()),
                GeneratedValue.ofValue(button.getUrl()),
                GeneratedValue.ofValue(true)
        );
    }

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

    public InlineKeyboardButton createInlineButton(ResourcePool pool){
        return InlineKeyboardButton.builder()
                .text(text.generate(pool))
                .url(url == null ? null : url.generate(pool))
                .callbackData(callbackData == null ? null : callbackData.generate(pool))
                .build();
    }

    public KeyboardButton createKeyboardButton(ResourcePool pool){
        return KeyboardButton.builder().text(text.generate(pool)).build();
    }
}
