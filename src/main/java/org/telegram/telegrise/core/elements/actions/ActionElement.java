package org.telegram.telegrise.core.elements.actions;

import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrise.core.elements.TranscriptionElement;

public interface ActionElement extends TranscriptionElement {
    PartialBotApiMethod<?> generateMethod();
}
