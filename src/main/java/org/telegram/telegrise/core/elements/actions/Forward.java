package org.telegram.telegrise.core.elements.actions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrise.ReturnConsumer;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.NodeElement;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;

@Element(name = "forward")
@Data @NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Forward extends NodeElement implements ActionElement{
    @Attribute(name = "chat")
    private GeneratedValue<Long> chatId;

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
    private GeneratedValue<ReturnConsumer> returnConsumer;

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
