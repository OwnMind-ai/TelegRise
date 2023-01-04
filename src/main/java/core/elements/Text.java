package core.elements;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Text implements TranscriptionElement{
    private final String text;
    private final String parseMode;
}
