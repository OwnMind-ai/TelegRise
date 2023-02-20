package org.telegram.telegrise.core.parser;

import org.telegram.telegrise.core.elements.BotTranscription;

public interface TranscriptionParser {
    BotTranscription parse() throws Exception;
}
