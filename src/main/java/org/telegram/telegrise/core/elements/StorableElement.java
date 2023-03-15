package org.telegram.telegrise.core.elements;

import org.telegram.telegrise.core.parser.ParserMemory;

public interface StorableElement extends TranscriptionElement{
    void store(ParserMemory memory);
}
