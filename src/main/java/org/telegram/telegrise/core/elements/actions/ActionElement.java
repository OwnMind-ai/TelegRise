package org.telegram.telegrise.core.elements.actions;

import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrise.utils.MessageUtils;
import org.telegram.telegrise.ReturnConsumer;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.TranscriptionElement;

import java.util.Objects;

public interface ActionElement extends TranscriptionElement {
    PartialBotApiMethod<?> generateMethod(ResourcePool resourcePool);
    GeneratedValue<Long> getChatId();
    default GeneratedValue<ReturnConsumer> getReturnConsumer() { return null; }

    default ReturnConsumer getConsumer(ResourcePool pool){
        return Objects.requireNonNullElse(generateNullableProperty(this.getReturnConsumer(), pool), null);
    }

    default Long generateChatId(ResourcePool pool){
        return getChatId() != null ? getChatId().generate(pool) : Objects.requireNonNull(MessageUtils.getChat(pool.getUpdate())).getId();
    }

}
