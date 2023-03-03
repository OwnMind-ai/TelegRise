package org.telegram.telegrise.core.elements.actions;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.TranscriptionElement;

public interface ActionElement extends TranscriptionElement {
    BotApiMethod<?> generateMethod(ResourcePool resourcePool);
}
