package org.telegram.telegrise.core.elements.actions;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrise.ReturnConsumer;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;

@Element(name = "chatAction")
@Data @NoArgsConstructor
public class ChatAction implements ActionElement{
    @Attribute(name = "chat")
    private GeneratedValue<Long> chatId;

    @Attribute(name = "messageThreadId")
    private GeneratedValue<Integer> messageThreadId;

    @Attribute(name = "action", nullable = false)
    private GeneratedValue<String> action;

    @Attribute(name = "returnConsumer")
    private GeneratedValue<ReturnConsumer> returnConsumer;

    @Override
    public PartialBotApiMethod<?> generateMethod(ResourcePool resourcePool) {
        return SendChatAction.builder()
                .chatId(generateChatId(resourcePool))
                .messageThreadId(generateNullableProperty(messageThreadId, resourcePool))
                .action(action.generate(resourcePool))
                .build();
    }
}
