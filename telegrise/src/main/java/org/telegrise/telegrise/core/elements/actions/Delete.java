package org.telegrise.telegrise.core.elements.actions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessages;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegrise.telegrise.utils.MessageUtils;

import java.util.List;
import java.util.Optional;

@Element(name = "delete")
@Getter @Setter @NoArgsConstructor
public class Delete extends ActionElement{
    @Attribute(name = "chat")
    private GeneratedValue<Long> chatId;

    @Attribute(name = "when")
    private GeneratedValue<Boolean> when;

    @Attribute(name = "messageId")
    private GeneratedValue<Integer> messageId;

    @Attribute(name = "registry")
    private GeneratedValue<String> registry;

    @Attribute(name = "onError")
    private GeneratedValue<Void> onError;

    @Override
    public PartialBotApiMethod<?> generateMethod(ResourcePool resourcePool) {
        if (registry != null) {
            String name = registry.generate(resourcePool);
            List<Message> register = resourcePool.getMemory().clearRegistry(name);
            
            if (register.isEmpty()) return null;
           
            Long chatId = this.generateChatId(resourcePool);
            return DeleteMessages.builder()
                    .chatId(chatId)
                    .messageIds(register.stream()
                        .peek(m -> { if (!m.getChatId().equals(chatId))  // TODO perhaps it is possible just to delete from these chats (groupingBy chatId and then execute deletes through pool.getSender())
                            throw new TelegRiseRuntimeException("Message from the register is in the different chat (%d) then specified (%d): %s".formatted(m.getChatId(), chatId, m), node); })
                        .map(Message::getMessageId).distinct().toList())
                    .build();
        }

        return DeleteMessage.builder()
                .chatId(this.generateChatId(resourcePool))
                .messageId(messageId != null ? messageId.generate(resourcePool) :
                        Optional.ofNullable(MessageUtils.getMessageId(resourcePool.getUpdate()))
                                .orElseThrow(() -> new TelegRiseRuntimeException("Unable to extract message ID for deletion action", node)))
                .build();
    }
}
