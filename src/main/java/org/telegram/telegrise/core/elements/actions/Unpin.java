package org.telegram.telegrise.core.elements.actions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.UnpinAllChatMessages;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.UnpinChatMessage;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;

@Element(name = "unpin")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Unpin extends ActionElement{
    @Attribute(name = "chat")
    private GeneratedValue<Long> chatId;

    @Attribute(name = "when")
    private GeneratedValue<Boolean> when;

    @Attribute(name = "messageId", nullable = false)
    private GeneratedValue<Integer> messageId;

    @Attribute(name = "all")
    private GeneratedValue<Boolean> all = GeneratedValue.ofValue(false);

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
