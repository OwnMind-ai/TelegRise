package org.telegram.telegrise.core.elements.actions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;

@Element(name = "pin")
@Data @NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Pin extends ActionElement{
    @Attribute(name = "chat")
    private GeneratedValue<Long> chatId;

    @Attribute(name = "messageId", nullable = false)
    private GeneratedValue<Integer> messageId;

    @Attribute(name = "disableNotification")
    private GeneratedValue<Boolean> disableNotification;

    @Override
    public PartialBotApiMethod<?> generateMethod(ResourcePool resourcePool) {
        return PinChatMessage.builder()
                .chatId(this.generateChatId(resourcePool))
                .messageId(messageId.generate(resourcePool))
                .disableNotification(generateNullableProperty(disableNotification, resourcePool))
                .build();
    }
}
