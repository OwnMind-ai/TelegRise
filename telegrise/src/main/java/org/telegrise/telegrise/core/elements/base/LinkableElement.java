package org.telegrise.telegrise.core.elements.base;

import org.telegrise.telegrise.core.elements.BotTranscription;

import java.util.function.Consumer;

public interface LinkableElement {
    default Consumer<BotTranscription> afterParsedTask() {
        return null;
    }
}
