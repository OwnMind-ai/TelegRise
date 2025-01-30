package org.telegrise.telegrise.core.elements;

import java.util.function.Consumer;

public interface LinkableElement {
    default Consumer<BotTranscription> afterParsedTask() {
        return null;
    }
}
