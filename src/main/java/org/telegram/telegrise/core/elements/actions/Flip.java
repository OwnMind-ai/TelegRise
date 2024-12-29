package org.telegram.telegrise.core.elements.actions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.TranscriptionMemory;
import org.telegram.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegram.telegrise.keyboard.KeyboardState;

@Element(name = "flip", finishAfterParsing = true)
@Getter @Setter @NoArgsConstructor
public class Flip extends ActionElement{
    @Attribute(name = "keyboard", nullable = false)
    private String keyboard;

    @Attribute(name = "when")
    private GeneratedValue<Boolean> when;

    @Attribute(name = "switch", nullable = false)
    private GeneratedValue<String> switchName;

    @Attribute(name = "onError")
    private GeneratedValue<Void> onError;

    @Override
    public PartialBotApiMethod<?> generateMethod(ResourcePool resourcePool) {
        KeyboardState state = resourcePool.getMemory().getKeyboardState(keyboard, parentTree);
        if (state == null) 
            throw new TelegRiseRuntimeException("Keyboard '" + keyboard + "' doesn't exist in current scope", node);
            
        String name = switchName.generate(resourcePool);

        if (state.getSwitchValue(name) == null)
            throw new TelegRiseRuntimeException("Unable to find switch '" + name, node);

        state.flipSwitch(name);

        return null;
    }

    @Override
    protected void validate(TranscriptionMemory memory) {
        if (!memory.containsKey(parentTree, keyboard)) {
            throw new TelegRiseRuntimeException("Keyboard '" + keyboard + "' doesn't exist in current scope", node);
        }
    }

    @Override
    public GeneratedValue<Long> getChatId() {
        return null;
    }
}
