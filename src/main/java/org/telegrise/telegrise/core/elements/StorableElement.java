package org.telegrise.telegrise.core.elements;

import org.telegrise.telegrise.core.parser.TranscriptionMemory;

public interface StorableElement {
    void store(TranscriptionMemory memory);
}
