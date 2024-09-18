package org.telegram.telegrise.core.elements.actions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessages;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegram.telegrise.utils.MessageUtils;

import java.util.List;
import java.util.Optional;

@Element(name = "delete")
@Data @NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Delete extends ActionElement{
    @Attribute(name = "chat")
    private GeneratedValue<Long> chatId;

    @Attribute(name = "when")
    private GeneratedValue<Boolean> when;

    @Attribute(name = "messageId")
    private GeneratedValue<Integer> messageId;

    @Attribute(name = "register")
    private GeneratedValue<String> register;

    @Override
    public PartialBotApiMethod<?> generateMethod(ResourcePool resourcePool) {
        if (register != null) {
            String name = register.generate(resourcePool);
            List<Message> register = resourcePool.getMemory().clearRegister(name);
            
            if (register.isEmpty()) return null;
           
            Long chatId = this.generateChatId(resourcePool);
            return DeleteMessages.builder()
                    .chatId(chatId)
                    .messageIds(register.stream()
                        .peek(m -> { if (!m.getChatId().equals(chatId)) 
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
