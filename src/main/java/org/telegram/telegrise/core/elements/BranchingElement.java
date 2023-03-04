package org.telegram.telegrise.core.elements;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrise.core.ResourcePool;

import java.util.List;

public interface BranchingElement extends TranscriptionElement{
    List<BotApiMethod<?>> getMethods(ResourcePool pool);
}
