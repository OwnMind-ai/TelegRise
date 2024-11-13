package org.telegram.telegrise.core.elements.keyboard;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegram.telegrise.keyboard.KeyboardState;

@Element(name = "switch")
@Getter @Setter @NoArgsConstructor
public class Switch extends Button{
    @Attribute(name = "name")
    private String name;

    @Attribute(name = "on", nullable = false)
    private GeneratedValue<String> onState;

    @Attribute(name = "off", nullable = false)
    private GeneratedValue<String> offState;

    @Attribute(name = "initial")
    private GeneratedValue<Boolean> initial = GeneratedValue.ofValue(false);

    @Attribute(name = "when")
    private GeneratedValue<Boolean> when;

    @Attribute(name = "explicit")
    private boolean explicit = false;     // If true, callback data will have "-on" or "-off"

    @Attribute(name = "prefix")
    private GeneratedValue<String> prefix = GeneratedValue.ofValue("switch-");

    @Override
    public InlineKeyboardButton createInlineButton(ResourcePool pool, KeyboardState keyboardState) {
        boolean enabled = keyboardState.getSwitchValue(name).equals(KeyboardState.SWITCH_ENABLED);

        return InlineKeyboardButton.builder()
                .callbackData(prefix.generate(pool) + name + (!explicit ? "" : enabled ? "-off" : "-on"))
                .text(enabled ? onState.generate(pool) : offState.generate(pool))
                .build();
    }

    @Override
    public KeyboardButton createKeyboardButton(ResourcePool pool) {
        throw new TelegRiseRuntimeException("Switch buttons are only allowed in inline keyboards", node);
    }
}