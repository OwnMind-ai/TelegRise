package org.telegram.telegrise.core.elements;

import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrise.core.ResourcePool;

import java.util.List;

public interface BranchingElement extends TranscriptionElement{
    List<PartialBotApiMethod<?>> getMethods(ResourcePool pool);
}
