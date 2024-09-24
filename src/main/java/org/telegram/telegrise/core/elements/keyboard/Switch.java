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

@Element(name = "switch")
@Getter @Setter @NoArgsConstructor
public class Switch extends Button{
    @Attribute(name = "name")
    private String name;

    @Attribute(name = "on", nullable = false)
    private GeneratedValue<String> onState;

    @Attribute(name = "off", nullable = false)
    private GeneratedValue<String> offState;

    @Attribute(name = "enabled")
    private GeneratedValue<Boolean> enabled = GeneratedValue.ofValue(false);

    @Attribute(name = "when")
    private GeneratedValue<Boolean> when = GeneratedValue.ofValue(true);

    @Override
    public InlineKeyboardButton createInlineButton(ResourcePool pool) {
        throw new TelegRiseRuntimeException("Switch buttons are only allowed in dynamic keyboards", node);
    }

    @Override
    public KeyboardButton createKeyboardButton(ResourcePool pool) {
        throw new TelegRiseRuntimeException("Switch buttons are only allowed in dynamic keyboards", node);
    }
}
