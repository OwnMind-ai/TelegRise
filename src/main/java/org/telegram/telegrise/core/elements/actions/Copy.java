package org.telegram.telegrise.core.elements.actions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.keyboard.Keyboard;
import org.telegram.telegrise.core.elements.text.Text;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.InnerElement;

import java.util.List;

@Element(name = "copy")
@Getter @Setter @NoArgsConstructor
public class Copy extends ActionElement{
    @Attribute(name = "chat")
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
    @Attribute(name = "replyTo")
    private GeneratedValue<Integer> replyTo;
    @Attribute(name = "allowSendingWithoutReply")
    private GeneratedValue<Boolean> allowSendingWithoutReply;

    @InnerElement
    private Text caption;
    @InnerElement
    private Keyboard keyboard;

    @Attribute(name = "returnConsumer")
    private GeneratedValue<Void> returnConsumer;

    @Attribute(name = "onError")
    private GeneratedValue<Void> onError;

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
                .replyToMessageId(generateNullableProperty(replyTo, pool))
                .allowSendingWithoutReply(generateNullableProperty(allowSendingWithoutReply, pool))
                .replyMarkup(this.keyboard != null ? this.keyboard.createMarkup(pool) : null)
                .build();
    }
}
