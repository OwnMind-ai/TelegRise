package org.telegram.telegrise.core.elements;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Text implements TranscriptionElement{
    private final String text;  //TODO variable support
    private final String parseMode;
}
