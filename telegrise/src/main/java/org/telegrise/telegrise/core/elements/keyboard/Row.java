package org.telegrise.telegrise.core.elements.keyboard;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegrise.telegrise.core.GeneratedValue;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.NodeElement;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.core.parser.InnerElement;
import org.telegrise.telegrise.keyboard.KeyboardState;

import java.util.ArrayList;
import java.util.List;

@Element(name = "row")
@Getter @Setter @NoArgsConstructor
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

    public KeyboardRow createKeyboardRow(ResourcePool pool, KeyboardState state, int rowIndex){
        List<KeyboardButton> buttonsArray = new ArrayList<>(buttons.size());

        for(int i = 0; i < buttons.size(); i++){
            Button b = buttons.get(i);
            if((state == null || state.isEnabled(rowIndex, i)) && Keyboard.filterKeyboardElement(b.getWhen(), b.getAccessLevel(), pool))
                buttonsArray.add(b.createKeyboardButton(pool));
        }

        return new KeyboardRow(buttonsArray);
    }

    public InlineKeyboardRow createInlineRow(ResourcePool pool, KeyboardState state, int rowIndex){
        List<InlineKeyboardButton> buttonsArray = new ArrayList<>(buttons.size());

        for(int i = 0; i < buttons.size(); i++){
            Button b = buttons.get(i);
            if((state == null || state.isEnabled(rowIndex, i)) && Keyboard.filterKeyboardElement(b.getWhen(), b.getAccessLevel(), pool))
                buttonsArray.add(b.createInlineButton(pool, state));
        }

        return new InlineKeyboardRow(buttonsArray);
    }
}
