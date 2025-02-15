package org.telegrise.telegrise.core.elements.actions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;


/**
 * Use this element to forward messages.
 * <p>
 * This element corresponds to the <a href="https://core.telegram.org/bots/api#forwardmessage">forwardMessage</a> method.
 * ChatId is <b>not</b >automatically extracted and must be specified.
 * It is required that this element has {@code chat}, {@code fromChat} and {@code messageId} attributes.
 *
 * @since 0.1
 * @see <a href="https://core.telegram.org/bots/api#forwardmessage">Telegram API: forwardMessage<a>
 */
@Element(name = "forward")
@Getter @Setter @NoArgsConstructor
public class Forward extends ActionElement{
    @Attribute(name = "chat", nullable = false)
    private GeneratedValue<Long> chatId;

    @Attribute(name = "when")
    private GeneratedValue<Boolean> when;

    @Attribute(name = "messageThreadId")
    private GeneratedValue<Integer> messageThreadId;

    @Attribute(name = "fromChat", nullable = false)
    private GeneratedValue<Long> fromChat;

    @Attribute(name = "messageId", nullable = false)
    private GeneratedValue<Integer> messageId;

    @Attribute(name = "disableNotification")
    private GeneratedValue<Boolean> disableNotification;

    @Attribute(name = "protectContent")
    private GeneratedValue<Boolean> protectContent;

    @Attribute(name = "returnConsumer")
    private GeneratedValue<Void> returnConsumer;

    @Attribute(name = "onError")
    private GeneratedValue<Void> onError;

    @Override
    public PartialBotApiMethod<?> generateMethod(ResourcePool pool) {
        return ForwardMessage.builder()
                .chatId(this.generateChatId(pool))
                .messageThreadId( generateNullableProperty(messageThreadId, pool))
                .fromChatId(fromChat.generate(pool))
                .messageId(messageId.generate(pool))
                .disableNotification( generateNullableProperty(disableNotification, pool))
                .protectContent( generateNullableProperty(protectContent, pool))
                .build();
    }
}
