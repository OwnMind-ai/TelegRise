package org.telegram.telegrise.core.elements;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrise.core.GeneratedValue;

import java.util.List;

@ToString
@AllArgsConstructor
public class Text implements TranscriptionElement{
    @Getter
    private final GeneratedValue<String> text;
    @Getter
    private GeneratedValue<String> parseMode;
    @Getter
    private GeneratedValue<List<MessageEntity>> entities;

    public Text(String text, String parseMode){
        this.text = GeneratedValue.ofValue(text);
        this.parseMode = GeneratedValue.ofValue(parseMode);
    }
}
