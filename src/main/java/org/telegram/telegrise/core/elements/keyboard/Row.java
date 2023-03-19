package org.telegram.telegrise.core.elements.keyboard;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.TranscriptionElement;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.InnerElement;

import java.util.List;
import java.util.stream.Collectors;

@Element(name = "row")
@Data @NoArgsConstructor
public class Row implements TranscriptionElement {
    @InnerElement(nullable = false)
    private List<Button> buttons;

    @Attribute(name = "when", expression = true)
    private GeneratedValue<Boolean> when = GeneratedValue.ofValue(true);

    public Row(List<Button> buttons){
        this.buttons = buttons;
    }

    public KeyboardRow createKeyboardRow(ResourcePool pool){
        return new KeyboardRow(this.buttons.stream().map(b -> b.createKeyboardButton(pool)).collect(Collectors.toList()));
    }

    public List<InlineKeyboardButton> createInlineRow(ResourcePool pool){
        return this.buttons.stream()
                .filter(b -> b.getWhen().generate(pool))
                .map(b -> b.createInlineButton(pool)).collect(Collectors.toList());
    }
}
