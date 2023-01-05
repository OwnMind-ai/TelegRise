package org.telegram.telegrise.core.elements;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.telegram.telegrise.core.GeneratedValue;

@ToString
@AllArgsConstructor
public class Text implements TranscriptionElement{
    @Getter
    private final GeneratedValue<String> text;
    @Getter
    private final GeneratedValue<String> parseMode;

    public Text(String text, String parseMode){
        this.text = GeneratedValue.ofValue(text);
        this.parseMode = GeneratedValue.ofValue(parseMode);
    }
}
