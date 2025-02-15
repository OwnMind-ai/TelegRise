package org.telegrise.telegrise.core.elements.actions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.core.parser.InnerElement;
import org.telegrise.telegrise.exceptions.TelegRiseInternalException;
import org.telegrise.telegrise.senders.UniversalSender;

import java.util.List;

/**
 * Represents a group of actions that executes the {@code ActionElement}s specified in its body.
 * The {@code when} attribute determines whether the action group should be executed, 
 * based on the provided condition.
 * 
 * <p>This class is particularly useful for grouping related actions and controlling 
 * their execution based on conditional logic.
 * 
 * @since 0.7
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
