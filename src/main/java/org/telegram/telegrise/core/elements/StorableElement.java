package org.telegram.telegrise.core.elements;

import org.telegram.telegrise.core.parser.TranscriptionMemory;

public interface StorableElement {
    void store(TranscriptionMemory memory);
}
