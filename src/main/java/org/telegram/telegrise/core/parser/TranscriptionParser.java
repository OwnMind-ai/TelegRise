package org.telegram.telegrise.core.parser;

import org.telegram.telegrise.core.BotTranscription;

public interface TranscriptionParser {
    BotTranscription parse() throws Exception;
}
