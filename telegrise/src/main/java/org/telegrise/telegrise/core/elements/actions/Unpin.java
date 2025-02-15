package org.telegrise.telegrise.core.elements.actions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.UnpinAllChatMessages;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.UnpinChatMessage;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;

/**
 * Use this method to remove a message from the list of pinned messages in a chat.
 * <p>
 * This element corresponds to the <a href="https://core.telegram.org/bots/api#unpinchatmessage">unpinChatMessage</a> method.
 * {@link org.telegrise.telegrise.utils.MessageUtils#getChat ChatId} is automatically extracted from the incoming update,
 * but can be specified if needed.
 * It is required that this element has {@code messageId} attribute.
 * <pre>
 * {@code
 * <unpin messageId="#getMessageId"/>
 * <unpin all="true"/>
 * }
 *
 * @since 0.1
 * @see <a href="https://core.telegram.org/bots/api#unpinchatmessage">Telegram API: unpinChatMessage<a>
 */
@Element(name = "unpin")
@Getter @Setter
@NoArgsConstructor
public class Unpin extends ActionElement{
    @Attribute(name = "chat")
    private GeneratedValue<Long> chatId;

    @Attribute(name = "when")
    private GeneratedValue<Boolean> when;

    @Attribute(name = "messageId", nullable = false)
    private GeneratedValue<Integer> messageId;

    @Attribute(name = "all")
    private GeneratedValue<Boolean> all = GeneratedValue.ofValue(false);

    @Attribute(name = "onError")
    private GeneratedValue<Void> onError;

    @Override
    public PartialBotApiMethod<?> generateMethod(ResourcePool resourcePool) {
        if (all.generate(resourcePool))
            return UnpinAllChatMessages.builder()
                    .chatId(this.generateChatId(resourcePool))
                    .build();
        else
            return UnpinChatMessage.builder()
                    .chatId(this.generateChatId(resourcePool))
                    .messageId(messageId.generate(resourcePool))
                    .build();
    }
}
