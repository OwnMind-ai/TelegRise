package org.telegram.telegrise;

import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.commands.DeleteMyCommands;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrise.core.elements.BotTranscription;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TelegramSessionsController {
    private final ThreadGroup threadGroup = new ThreadGroup(this.getClass().getSimpleName());
    private final ConcurrentMap<UserIdentifier, UserSession> sessions = new ConcurrentHashMap<>();
    @Getter
    private final BotTranscription transcription;
    private final RoleProvider roleProvider;
    @Setter
    private DefaultAbsSender sender;
    private final List<Class<? extends PrimaryHandler>> handlersClasses;

    public TelegramSessionsController(BotTranscription transcription, RoleProvider roleProvider, List<Class<? extends PrimaryHandler>> handlersClasses) {
        this.transcription = transcription;
        this.roleProvider = roleProvider;
        this.handlersClasses = handlersClasses;
    }

    public void initialize(){
        assert sender != null;

        if(this.transcription.getRootMenu().getChatTypes() == null)
            this.transcription.getRootMenu().setChatTypes(new String[]{ChatTypes.ALL});

        if (Boolean.parseBoolean(this.transcription.getAutoCommands())) {
            try {
                this.sender.execute(new DeleteMyCommands());
            } catch (TelegramApiException e) { throw new RuntimeException(e); }

            ChatTypes.GENERAL_SCOPES_LIST.forEach(l -> {
                SetMyCommands setMyCommands = this.transcription.getSetCommands(l);

                try {
                    if (!setMyCommands.getCommands().isEmpty())
                        this.sender.execute(setMyCommands);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            });
        }
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
        if (!(memory instanceof SessionMemoryImpl))
            throw new TelegRiseRuntimeException("Unable to load session with third-party implementation");

        SessionMemoryImpl sessionMemory = (SessionMemoryImpl) memory;
        UserSession session = new UserSession(sessionMemory.getUserIdentifier(), sessionMemory, transcription, sender);

        this.sessions.put(sessionMemory.getUserIdentifier(), session);
    }

    private void createSession(UserIdentifier identifier) {
        UserSession session = new UserSession(identifier, this.transcription, this.sender);
        session.setRoleProvider(this.roleProvider);
        session.addHandlersClasses(this.handlersClasses);
        this.sessions.put(identifier, session);
    }

    private void updateSession(UserSession session, Update update){
        session.update(update);

        if (!session.isRunning())
            new Thread(this.threadGroup, session).start();
    }
}
