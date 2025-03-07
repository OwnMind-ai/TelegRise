package org.telegrise.telegrise.core.elements.actions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.core.parser.TranscriptionMemory;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegrise.telegrise.keyboard.KeyboardState;

/**
 * Use this element to <i>flip</i> (set to the opposite) the value of the switch in the specified keyboard.
 * This element doesn't make any API calls.
 * <pre>
 * {@code
 * <flip keyboard="keyboardName" switch="switchName"/>
 * }
 * </pre>
 *
 * @since 0.1
 */
@Element(name = "flip", finishAfterParsing = true)
@Getter @Setter @NoArgsConstructor
public class Flip extends ActionElement{
    /**
     * Name of the keyboard that contains a switch
     */
    @Attribute(name = "keyboard", nullable = false)
    private GeneratedValue<String> keyboard;

    /**
     * Name of the switch to be flipped
     */
    @Attribute(name = "switch", nullable = false)
    private GeneratedValue<String> switchName;

    /**
     * Determines if this element must be executed (if returns {@code true})
     */
    @Attribute(name = "when")
    private GeneratedValue<Boolean> when;

    @Override
    public PartialBotApiMethod<?> generateMethod(ResourcePool resourcePool) {
        String keyboardName = keyboard.generate(resourcePool);
        KeyboardState state = resourcePool.getMemory().getKeyboardState(keyboardName, parentTree);
        if (state == null) 
            throw new TelegRiseRuntimeException("Keyboard '" + keyboardName + "' doesn't exist in current scope", node);
            
        String name = switchName.generate(resourcePool);

        if (state.getSwitchValue(name) == null)
            throw new TelegRiseRuntimeException("Unable to find switch '" + name, node);

        state.flipSwitch(name);

        return null;
    }

    @Override
    protected void validate(TranscriptionMemory memory) {
        if (!keyboard.validate(s -> memory.containsKey(parentTree, s))) {
            throw new TelegRiseRuntimeException("Keyboard '" + keyboard + "' doesn't exist in current scope", node);
        }
    }

    @Override
    public GeneratedValue<Long> getChatId() {
        return null;
    }
}
