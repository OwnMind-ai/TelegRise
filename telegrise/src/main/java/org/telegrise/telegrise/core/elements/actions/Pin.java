package org.telegrise.telegrise.core.elements.actions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;

/**
 * Use this method to add a message to the list of pinned messages in a chat.
 * <p>
 * This element corresponds to the <a href="https://core.telegram.org/bots/api#pinchatmessage">pinChatMessage</a> method.
 * {@link org.telegrise.telegrise.utils.MessageUtils#getChat ChatId} is automatically extracted from the incoming update,
 * but can be specified if needed.
 * It is required that this element has {@code messageId} attribute.
 * <pre>
 * {@code
 * <pin messageId="#getMessageId"/>
 * }
 *
 * @since 0.1
 * @see <a href="https://core.telegram.org/bots/api#pinchatmessage">Telegram API: pinChatMessage<a>
 */
@Element(name = "pin")
@Getter @Setter @NoArgsConstructor
public class Pin extends ActionElement{
    @Attribute(name = "chat")
    private GeneratedValue<Long> chatId;

    @Attribute(name = "when")
    private GeneratedValue<Boolean> when;

    @Attribute(name = "messageId", nullable = false)
    private GeneratedValue<Integer> messageId;

    @Attribute(name = "disableNotification")
    private GeneratedValue<Boolean> disableNotification;

    @Attribute(name = "onError")
    private GeneratedValue<Void> onError;

    @Override
    public PartialBotApiMethod<?> generateMethod(ResourcePool resourcePool) {
        return PinChatMessage.builder()
                .chatId(this.generateChatId(resourcePool))
                .messageId(messageId.generate(resourcePool))
                .disableNotification(generateNullableProperty(disableNotification, resourcePool))
                .build();
    }
}
