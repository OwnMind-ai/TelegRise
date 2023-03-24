package org.telegram.telegrise.core.elements;

import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.parser.TranscriptionMemory;

import java.util.List;

public interface BranchingElement extends StorableElement{
    List<PartialBotApiMethod<?>> getMethods(ResourcePool pool);

    String getName();
    String[] getChatTypes();

    @Override
    default void store(TranscriptionMemory memory) {
        memory.put(this.getName(), this);
    }
}
