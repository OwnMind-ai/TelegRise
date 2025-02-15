package org.telegrise.telegrise.core.elements.base;

import org.telegrise.telegrise.core.elements.BotTranscription;

import java.util.function.Consumer;

/**
 * Represents an element that can be linked using {@code <link src="..."/>}.
 *
 * @since 0.2
 */
public interface LinkableElement {
    default Consumer<BotTranscription> afterParsedTask() {
        return null;
    }
}