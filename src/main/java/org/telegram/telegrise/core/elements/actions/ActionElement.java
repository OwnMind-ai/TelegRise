package org.telegram.telegrise.core.elements.actions;

import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrise.MessageUtils;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.TranscriptionElement;

import java.util.Objects;

public interface ActionElement extends TranscriptionElement {
    PartialBotApiMethod<?> generateMethod(ResourcePool resourcePool);
    GeneratedValue<Long> getChatId();

    default Long generateChatId(ResourcePool pool){
        return getChatId() != null ? getChatId().generate(pool) : Objects.requireNonNull(MessageUtils.getChat(pool.getUpdate())).getId();
    }

}
