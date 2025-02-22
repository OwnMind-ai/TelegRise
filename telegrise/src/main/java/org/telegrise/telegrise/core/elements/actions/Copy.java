package org.telegrise.telegrise.core.elements.actions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.ReplyParameters;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.keyboard.Keyboard;
import org.telegrise.telegrise.core.elements.meta.Reply;
import org.telegrise.telegrise.core.elements.text.Text;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.core.parser.InnerElement;
import org.telegrise.telegrise.types.ApiResponse;

import java.util.List;

/**
 * Use this element to copy messages.
 * <p>
 * This element corresponds to the <a href="https://core.telegram.org/bots/api#cpoymessage">copyMessage</a> method.
 * ChatId is <b>not</b >automatically extracted and must be specified.
 * It is required that this element has {@code chat}, {@code fromChat} and {@code messageId} attributes.
 *
 * @since 0.1
 * @see <a href="https://core.telegram.org/bots/api#copymessage">Telegram API: copyMessage</a>
 */
@Element(name = "copy")
@Getter @Setter @NoArgsConstructor
public class Copy extends ActionElement{
    /**
     * Unique identifier for the target chat.
     */
    @Attribute(name = "chat")
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
     * Identifier of the message that will be replied to in the current chat.
     * This is a shortened version of {@code <reply to="..."/>}.
     */
    @Attribute(name = "replyTo")
    private GeneratedValue<Integer> replyTo;

    /**
     * Set to true if a message should be sent even if the specified message to be replied to is not found.
     * This is a shortened version of {@code <reply allowSendingWithoutReply="true"/>}.
     */
    @Attribute(name = "allowSendingWithoutReply")
    private GeneratedValue<Boolean> allowSendingWithoutReply;

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

    @InnerElement
    private Text caption;
    @InnerElement
    private Keyboard keyboard;

    @InnerElement
    private Reply replyParameters;

    @Override
    public PartialBotApiMethod<?> generateMethod(ResourcePool pool) {
        return CopyMessage.builder()
                .chatId(this.generateChatId(pool))
                .messageThreadId( generateNullableProperty(messageThreadId, pool))
                .fromChatId(fromChat.generate(pool))
                .messageId(messageId.generate(pool))
                .caption(this.caption != null ? caption.generateText(pool) : null)
                .parseMode(this.caption != null ? generateNullableProperty(this.caption.getParseMode(), pool) : null)
                .captionEntities(this.caption != null ? generateNullableProperty(this.caption.getEntities(), List.of(), pool) : List.of())
                .disableNotification( generateNullableProperty(disableNotification, pool))
                .protectContent( generateNullableProperty(protectContent, pool))
                .replyMarkup(this.keyboard != null ? this.keyboard.createMarkup(pool) : null)
                .replyParameters(this.createReplyParameters(pool))
                .build();
    }

    public ReplyParameters createReplyParameters(ResourcePool pool) {
        if (replyParameters != null) return replyParameters.produceReplyParameters(pool);
        else if (replyTo != null) {
            return ReplyParameters.builder()
                    .messageId(replyTo.generate(pool))
                    .allowSendingWithoutReply(GeneratedValue.generate(allowSendingWithoutReply, pool))
                    .build();
        }

        return null;
    }
}
