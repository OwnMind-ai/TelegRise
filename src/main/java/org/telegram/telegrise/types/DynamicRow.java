package org.telegram.telegrise.types;

import lombok.Data;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.keyboard.Row;
import org.telegram.telegrise.core.elements.keyboard.Switch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class DynamicRow implements Serializable {

    public static DynamicRow ofRow(Row row){
        return new DynamicRow(row.getButtons().stream()
                .map(button -> button instanceof Switch ? SwitchButton.ofSwitch((Switch) button) : DynamicButton.ofButton(button))
                .collect(Collectors.toList()), row.getWhen());
    }

    private final List<DynamicButton> buttons;
    private GeneratedValue<Boolean> when;

    public DynamicRow(GeneratedValue<Boolean> when) {
        this.when = when;
        this.buttons = new ArrayList<>();
    }

    public DynamicRow(List<DynamicButton> buttons, GeneratedValue<Boolean> when){
        this.buttons = new ArrayList<>(buttons);
        this.when = when;
    }

    public KeyboardRow createKeyboardRow(ResourcePool pool){
        return new KeyboardRow(this.buttons.stream()
                .filter(b -> b.getWhen().generate(pool))
                .map(b -> b.createKeyboardButton(pool)).collect(Collectors.toList()));
    }

    public List<InlineKeyboardButton> createInlineRow(ResourcePool pool){
        return this.buttons.stream()
                .filter(b -> b.getWhen().generate(pool))
                .map(b -> b.createInlineButton(pool)).collect(Collectors.toList());
    }
}
