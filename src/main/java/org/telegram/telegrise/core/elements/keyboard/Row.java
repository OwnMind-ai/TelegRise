package org.telegram.telegrise.core.elements.keyboard;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.NodeElement;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.InnerElement;

import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = false)
@Element(name = "row")
@Data @NoArgsConstructor
public class Row extends NodeElement {
    @InnerElement(nullable = false)
    private List<Button> buttons;

    @Attribute(name = "when")
    private GeneratedValue<Boolean> when;

    @Attribute(name = "accessLevel")
    private Integer accessLevel;

    public Row(List<Button> buttons){
        this.buttons = buttons;
    }

    public KeyboardRow createKeyboardRow(ResourcePool pool){
        return new KeyboardRow(this.buttons.stream()
                .filter(b -> Keyboard.filterKeyboardElement(b.getWhen(), b.getAccessLevel(), pool))
                .map(b -> b.createKeyboardButton(pool)).collect(Collectors.toList()));
    }

    public InlineKeyboardRow createInlineRow(ResourcePool pool){
        return new InlineKeyboardRow(this.buttons.stream()
                .filter(b -> Keyboard.filterKeyboardElement(b.getWhen(), b.getAccessLevel(), pool))
                .map(b -> b.createInlineButton(pool)).collect(Collectors.toList()));
    }
}
