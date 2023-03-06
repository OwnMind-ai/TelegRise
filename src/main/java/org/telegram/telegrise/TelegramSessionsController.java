package org.telegram.telegrise;

import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrise.core.elements.BotTranscription;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TelegramSessionsController {
    private final ThreadGroup threadGroup = new ThreadGroup(this.getClass().getSimpleName());
    private final ConcurrentMap<UserIdentifier, UserSession> sessions = new ConcurrentHashMap<>();
    @Getter
    private final BotTranscription transcription;
    @Setter
    private DefaultAbsSender sender;

    public TelegramSessionsController(BotTranscription transcription) {
        this.transcription = transcription;
    }

    public void onUpdateReceived(Update update){
        User from = MessageUtils.getFrom(update);

        if (from != null && !from.getIsBot()){
            UserIdentifier identifier = UserIdentifier.of(from);

            if (!sessions.containsKey(identifier))
                this.createSession(identifier);

            this.updateSession(this.sessions.get(identifier), update);
        }
    }

    //TODO custom session loader
    public void loadSession(SessionMemory memory){
        if (!(memory instanceof SessionMemoryImpl))  //FIXME
            throw new TelegRiseRuntimeException("Unable to load session with third-party implementation");

        SessionMemoryImpl sessionMemory = (SessionMemoryImpl) memory;
        UserSession session = new UserSession(sessionMemory.getUserIdentifier(), sessionMemory, transcription, sender);

        this.sessions.put(sessionMemory.getUserIdentifier(), session);
    }

    private void createSession(UserIdentifier identifier) {
        UserSession session = new UserSession(identifier, this.transcription, this.sender);
        this.sessions.put(identifier, session);
    }

    private void updateSession(UserSession session, Update update){
        session.update(update);

        if (!session.isRunning())
            new Thread(this.threadGroup, session).start();
    }
}
