package org.telegram.telegrise.core.elements.actions;

import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrise.ReturnConsumer;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.NodeElement;
import org.telegram.telegrise.utils.MessageUtils;

import java.util.Objects;

public abstract class ActionElement extends NodeElement {
    public abstract PartialBotApiMethod<?> generateMethod(ResourcePool resourcePool);
    public abstract GeneratedValue<Long> getChatId();
    public GeneratedValue<ReturnConsumer> getReturnConsumer() { return null; }
    public abstract GeneratedValue<Boolean> getWhen();

    public ReturnConsumer getConsumer(ResourcePool pool){
        return Objects.requireNonNullElse(generateNullableProperty(this.getReturnConsumer(), pool), null);
    }

    public Long generateChatId(ResourcePool pool){
        return getChatId() != null ? getChatId().generate(pool) : Objects.requireNonNull(MessageUtils.getChat(pool.getUpdate())).getId();
    }
}