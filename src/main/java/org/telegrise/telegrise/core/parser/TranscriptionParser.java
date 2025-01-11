package org.telegrise.telegrise.core.parser;

import org.telegrise.telegrise.core.elements.BotTranscription;

public interface TranscriptionParser {
    BotTranscription parse() throws Exception;
}
