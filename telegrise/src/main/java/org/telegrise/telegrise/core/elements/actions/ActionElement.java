package org.telegrise.telegrise.core.elements.actions;

import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegrise.telegrise.core.GeneratedValue;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.NodeElement;
import org.telegrise.telegrise.core.elements.StorableElement;
import org.telegrise.telegrise.core.parser.TranscriptionMemory;
import org.telegrise.telegrise.utils.MessageUtils;

import java.util.Objects;

/**
 * The base class for all elements that represent executable action and can (but not required to) produce <code>PartialBotApiMethod</code> for Telegram client execution.
 * 
 * @since 0.1.0
 */
public abstract class ActionElement extends NodeElement implements StorableElement {
    public abstract PartialBotApiMethod<?> generateMethod(ResourcePool resourcePool);
    public abstract GeneratedValue<Long> getChatId();
    public GeneratedValue<Void> getReturnConsumer() { return null; }
    public abstract GeneratedValue<Boolean> getWhen();
    public GeneratedValue<Void> getOnError() { return null; }

    // Used for <transition edit="..."/>
    public Edit toEdit(){
        return null;
    }

    public String getName(){
        return null;
    }

    @Override
    public void store(TranscriptionMemory memory) {
        if(getName() != null)
            memory.put(this.getParentTree(), getName(), this);
    }

    public Long generateChatId(ResourcePool pool){
        return getChatId() != null ? getChatId().generate(pool) : Objects.requireNonNull(MessageUtils.getChat(pool.getUpdate())).getId();
    }
}