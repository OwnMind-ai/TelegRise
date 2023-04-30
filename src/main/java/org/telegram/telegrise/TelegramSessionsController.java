package org.telegram.telegrise;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.math.NumberUtils;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.commands.DeleteMyCommands;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrise.core.elements.BotTranscription;
import org.telegram.telegrise.resources.ResourceFactory;

import java.util.List;
import java.util.concurrent.*;

public class TelegramSessionsController {
    private static final int DEFAULT_THREAD_POOL_SIZE = 128;

    private final ExecutorService poolExecutor;
    private final ConcurrentMap<UserIdentifier, UserSession> sessions = new ConcurrentHashMap<>();
    @Getter
    private final BotTranscription transcription;
    private final RoleProvider roleProvider;
    private final List<ResourceFactory<?>> resourceFactories;
    @Setter
    private DefaultAbsSender sender;
    private final List<Class<? extends PrimaryHandler>> handlersClasses;

    public TelegramSessionsController(BotTranscription transcription, RoleProvider roleProvider, List<ResourceFactory<?>> resourceFactories, List<Class<? extends PrimaryHandler>> handlersClasses) {
        this.transcription = transcription;
        this.roleProvider = roleProvider;
        this.resourceFactories = resourceFactories;
        this.handlersClasses = handlersClasses;
        this.poolExecutor = this.createExecutorService();
    }

    private ExecutorService createExecutorService(){
        String property = System.getProperty("telegrise.threadPoolSize");

        if(NumberUtils.isDigits(property))
            return Executors.newFixedThreadPool(Integer.parseInt(property));

        return Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
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
        session.getResourceInjector().addFactories(resourceFactories);
        session.setRoleProvider(this.roleProvider);
        session.addHandlersClasses(this.handlersClasses);
        this.sessions.put(identifier, session);
    }

    private void updateSession(UserSession session, Update update){
        session.update(update);

        if (!session.isRunning())
            poolExecutor.submit(session);
    }
}
