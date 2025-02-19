package org.telegrise.telegrise.core.elements.actions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.types.ApiResponse;


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
    /**
     * Unique identifier for the target chat.
     */
    @Attribute(name = "chat", nullable = false)
    private GeneratedValue<Long> chatId;

    /**
     * Determines if this element must be executed (if returns {@code true})
     */
    @Attribute(name = "when")
    private GeneratedValue<Boolean> when;

    /**
     * Unique identifier for the target message thread (topic) of the forum.
     */
    @Attribute(name = "messageThreadId")
    private GeneratedValue<Integer> messageThreadId;

    /**
     * Unique identifier for the chat where the original message was sent.
     */
    @Attribute(name = "fromChat", nullable = false)
    private GeneratedValue<Long> fromChat;

    /**
     * Message identifier in the chat specified in {@code fromChat}
     */
    @Attribute(name = "messageId", nullable = false)
    private GeneratedValue<Integer> messageId;

    /**
     * Sends the message silently. Users will receive a notification with no sound.
     */
    @Attribute(name = "disableNotification")
    private GeneratedValue<Boolean> disableNotification;

    /**
     * Protects the contents of the sent message from forwarding and saving.
     */
    @Attribute(name = "protectContent")
    private GeneratedValue<Boolean> protectContent;

    /**
     * Provides a result of the send method.
     * Java expressions can use variable {@code message},
     * Method references can use parameter of type
     * {@link Message} or {@link ApiResponse} to access returned value.
     */
    @Attribute(name = "returnConsumer")
    private GeneratedValue<Void> returnConsumer;

    /**
     * Specified expression is invoked when an API error occurs; exception will not be thrown.
     * Referenced method can use parameter of type {@link TelegramApiException} to handle the exception.
     */
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
