package org.telegram.telegrise.keyboard;

import lombok.Data;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.keyboard.Row;
import org.telegram.telegrise.core.elements.keyboard.Switch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents a dynamic buttons row that used in <a href="#{@link}">{@link org.telegram.telegrise.keyboard.DynamicKeyboard DynamicKeyboard}</a>.
 * Dynamic rows can be dynamically disabled or enabled based on specified conditions that can be set using <code>setWhen</code> method.
 * 
 * @see DynamicButton
 * @see DynamicKeyboard
 * @since 0.4
 */
@Data
public class DynamicRow implements Serializable {
    /**
    * Creates a dynamic row from a <a href="#{@link}">{@link org.telegram.telegrise.core.elements.keyboard.Row Row}</a> object.
    * 
    * @param row The Row object to create a dynamic row from.
    * @param pool The ResourcePool that required to create the <code>SwitchButton</code>.
    * @return The created DynamicRow object.
    */
    public static DynamicRow ofRow(Row row, ResourcePool pool){
        return new DynamicRow(row.getButtons().stream()
                .map(button -> button instanceof Switch ? SwitchButton.ofSwitch((Switch) button, pool) : DynamicButton.ofButton(button))
                .collect(Collectors.toList()),
                    Objects.requireNonNullElse(row.getWhen(), GeneratedValue.ofValue(true)));
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

    /**
    * Creates an KeyboardRow object using the dynamic row's properties
    * and a given <code>ResourcePool</code>.
    * 
    * @param pool The ResourcePool object to use for creating the KeyboardRow.
    * @return The created KeyboardRow object.
    * @see org.telegram.telegrise.core.ResourcePool
    */
    public KeyboardRow createKeyboardRow(ResourcePool pool){
        return new KeyboardRow(this.buttons.stream()
                .filter(b -> b.getWhen().generate(pool))
                .map(b -> b.createKeyboardButton(pool)).collect(Collectors.toList()));
    }

    /**
    * Creates a list of InlineKeyboardButton objects using the dynamic row's properties
    * and a given <code>ResourcePool</code>.
    * 
    * @param pool The ResourcePool object.
    * @return The created list of InlineKeyboardButton objects.
    * @see org.telegram.telegrise.core.ResourcePool
    */
    public InlineKeyboardRow createInlineRow(ResourcePool pool){
        return new InlineKeyboardRow(this.buttons.stream()
                .filter(b -> b.getWhen().generate(pool))
                .map(b -> b.createInlineButton(pool)).collect(Collectors.toList()));
    }
}
