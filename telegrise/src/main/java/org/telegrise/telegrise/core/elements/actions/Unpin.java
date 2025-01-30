package org.telegrise.telegrise.core.elements.actions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.UnpinAllChatMessages;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.UnpinChatMessage;
import org.telegrise.telegrise.core.GeneratedValue;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;

@Element(name = "unpin")
@Getter @Setter
@NoArgsConstructor
public class Unpin extends ActionElement{
    @Attribute(name = "chat")
    private GeneratedValue<Long> chatId;

    @Attribute(name = "when")
    private GeneratedValue<Boolean> when;

    @Attribute(name = "messageId", nullable = false)
    private GeneratedValue<Integer> messageId;

    @Attribute(name = "all")
    private GeneratedValue<Boolean> all = GeneratedValue.ofValue(false);

    @Attribute(name = "onError")
    private GeneratedValue<Void> onError;

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
