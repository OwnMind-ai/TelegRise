package org.telegram.telegrise.core.elements.actions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.NodeElement;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegram.telegrise.keyboard.DynamicKeyboard;
import org.telegram.telegrise.keyboard.SwitchButton;

@Element(name = "flip")
@Data @NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Flip extends NodeElement implements ActionElement{
    @Attribute(name = "id", nullable = false)
    private String id;

    @Attribute(name = "switch", nullable = false)
    private GeneratedValue<String> switchName;

    @Override
    public PartialBotApiMethod<?> generateMethod(ResourcePool resourcePool) {
        DynamicKeyboard keyboard = resourcePool.getMemory().get(this.id, DynamicKeyboard.class);
        String name = switchName.generate(resourcePool);
        SwitchButton switchButton = keyboard.getSwitch(name);

        if (switchButton == null)
            throw new TelegRiseRuntimeException("Unable to find switch '" + name);

        switchButton.flip(resourcePool);

        return null;
    }

    @Override
    public GeneratedValue<Long> getChatId() {
        return null;
    }
}
