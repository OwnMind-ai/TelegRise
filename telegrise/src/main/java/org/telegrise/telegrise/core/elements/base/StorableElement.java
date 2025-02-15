package org.telegrise.telegrise.core.elements.base;

import org.telegrise.telegrise.core.parser.TranscriptionMemory;

/**
 * Represents an element that can be stored in {@link TranscriptionMemory}.
 * Unlike {@link NamedElement},
 * implementation of this interface can specify custom logic for storing method.
 *
 * @since 0.10
 */
public interface StorableElement {
    /**
     * Implementation of this method stores this element into memory with some logic and conditions if required.
     * @param memory transcription memory
     */
    void store(TranscriptionMemory memory);
}
