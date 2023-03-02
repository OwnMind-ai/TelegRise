package org.telegram.telegrise;

import org.telegram.telegrise.core.elements.BotTranscription;

public class UserSession implements Runnable{
    private final SessionMemory sessionMemory;
    private final BotTranscription transcription;

    public UserSession(BotTranscription transcription) {
        this.sessionMemory = new SessionMemory(transcription.hashCode());
        this.transcription = transcription;
    }

    @Override
    public void run() {

    }
}
