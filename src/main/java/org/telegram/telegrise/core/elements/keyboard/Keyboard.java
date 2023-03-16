package org.telegram.telegrise.core.elements.keyboard;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrise.TelegRiseRuntimeException;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.StorableElement;
import org.telegram.telegrise.core.elements.TranscriptionElement;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.ElementField;
import org.telegram.telegrise.core.parser.InnerElement;
import org.telegram.telegrise.core.parser.ParserMemory;

import java.util.List;
import java.util.stream.Collectors;

@Element(name = "keyboard")
@Data @NoArgsConstructor
public class Keyboard implements StorableElement, TranscriptionElement {
    public static final String INLINE = "inline";
    public static final String REPLY = "reply";

    @ElementField(name = "type")
    private String type;

    @ElementField(name = "name")
    private String name;

    @InnerElement
    private List<Row> rows;

    @Override
    public void store(ParserMemory memory) {
        assert name != null : "Unable to store keyboard without name";
        memory.put(name, this);
    }

    public ReplyKeyboard createMarkup(ResourcePool pool){
        switch (type.toLowerCase()) {
            case INLINE:
                return new InlineKeyboardMarkup(rows.stream().map(r -> r.createInlineRow(pool)).collect(Collectors.toList()));
            case REPLY:
                return new ReplyKeyboardMarkup(rows.stream().map(r -> r.createKeyboardRow(pool)).collect(Collectors.toList()));
            default:
                throw new TelegRiseRuntimeException("Undefined keyboard type '" + type + "'");
        }
    }

}
