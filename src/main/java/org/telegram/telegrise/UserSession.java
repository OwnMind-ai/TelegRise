package org.telegram.telegrise;

import lombok.Getter;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrise.core.elements.BotTranscription;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

public class UserSession implements Runnable{
    private final UserIdentifier userIdentifier;
    private final SessionMemory sessionMemory;
    private final BotTranscription transcription;
    private final DefaultAbsSender sender;
    @Getter
    private final Deque<TreeExecutor> treeExecutors = new ConcurrentLinkedDeque<>();

    public UserSession(UserIdentifier userIdentifier, BotTranscription transcription, DefaultAbsSender sender) {
        this.userIdentifier = userIdentifier;
        this.sessionMemory = new SessionMemory(transcription.hashCode());
        this.transcription = transcription;
        this.sender = sender;
    }

    public UserSession(UserIdentifier userIdentifier, SessionMemory sessionMemory, BotTranscription transcription, DefaultAbsSender sender) {
        this.userIdentifier = userIdentifier;
        this.sender = sender;

        if (sessionMemory.getTranscriptionHashcode() == transcription.hashCode()){
            this.sessionMemory = sessionMemory;
            this.transcription = transcription;
        } else
            throw new TelegRiseRuntimeException("Loaded SessionMemory object relates to another bot transcription");
    }

    @Override
    public void run() {

    }

    public void handleUpdate(Update update) {

    }
}
