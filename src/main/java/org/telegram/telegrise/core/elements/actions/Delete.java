package org.telegram.telegrise.core.elements.actions;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrise.utils.MessageUtils;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.exceptions.TelegRiseRuntimeException;

import java.util.Optional;

@Element(name = "delete")
@Data @NoArgsConstructor
public class Delete implements ActionElement{
    @Attribute(name = "chat")
    private GeneratedValue<Long> chatId;

    @Attribute(name = "messageId")
    private GeneratedValue<Integer> messageId;

    @Override
    public PartialBotApiMethod<?> generateMethod(ResourcePool resourcePool) {
        return DeleteMessage.builder()
                .chatId(this.generateChatId(resourcePool))
                .messageId(messageId != null ? messageId.generate(resourcePool) :
                        Optional.ofNullable(MessageUtils.getMessageId(resourcePool.getUpdate()))
                                .orElseThrow(() -> new TelegRiseRuntimeException("Unable to extract message ID for deletion action")))
                .build();
    }
}
