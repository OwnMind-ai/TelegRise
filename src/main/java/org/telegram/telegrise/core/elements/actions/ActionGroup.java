package org.telegram.telegrise.core.elements.actions;

import java.util.List;

import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.InnerElement;
import org.telegram.telegrise.exceptions.TelegRiseInternalException;
import org.telegram.telegrise.senders.UniversalSender;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * Represents a group of actions that executes the {@code ActionElement}s specified in its body.
 * The {@code when} attribute determines whether the action group should be executed, 
 * based on the provided condition.
 * 
 * <p>This class is particularly useful for grouping related actions and controlling 
 * their execution based on conditional logic.
 * 
 * @since 0.7.5
 */
@Element(name = "actionGroup")
@Getter @Setter @NoArgsConstructor
public class ActionGroup extends ActionElement{
    @Attribute(name = "when")
    private GeneratedValue<Boolean> when;

    @InnerElement(nullable = false)
    private List<ActionElement> actions;

    @Override
    public PartialBotApiMethod<?> generateMethod(ResourcePool resourcePool) {
        UniversalSender sender = new UniversalSender(resourcePool.getSender());
        
        actions.forEach(a -> {
            try {
                sender.execute(a, resourcePool);
            } catch (TelegramApiException e) {
                throw new TelegRiseInternalException(e);
            }
        });

        return null;
    }

    @Override
    public GeneratedValue<Long> getChatId() {
        throw new UnsupportedOperationException("Action group cannot contain chat id");
    }
}
