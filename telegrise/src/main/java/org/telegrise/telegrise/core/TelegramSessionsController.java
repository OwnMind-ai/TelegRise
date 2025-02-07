package org.telegrise.telegrise.core;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.telegram.telegrambots.meta.api.methods.GetMe;
import org.telegram.telegrambots.meta.api.methods.commands.DeleteMyCommands;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegrise.telegrise.*;
import org.telegrise.telegrise.annotations.Handler;
import org.telegrise.telegrise.core.elements.BotTranscription;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegrise.telegrise.resources.ResourceFactory;
import org.telegrise.telegrise.senders.BotSender;
import org.telegrise.telegrise.types.ChatTypes;
import org.telegrise.telegrise.utils.MessageUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Slf4j
public class TelegramSessionsController implements SessionsManager {
    private final ConcurrentMap<SessionIdentifier, UserSession> sessions = new ConcurrentHashMap<>();
    @Getter
    private final BotTranscription transcription;
    @Setter
    private RoleProvider roleProvider;
    @Setter
    private SessionInitializer sessionInitializer;
    private final List<ResourceFactory<?>> resourceFactories;

    @Setter
    private TelegramClient client;
    private List<Class<? extends UpdateHandler>> userHandlersClasses;
    private UpdateHandlersController handlersController;

    public TelegramSessionsController(BotTranscription transcription, List<ResourceFactory<?>> resourceFactories, List<Class<? extends UpdateHandler>> handlersClasses) {
        this.transcription = transcription;
        this.resourceFactories = resourceFactories;
        this.handlersController = new UpdateHandlersController(null);
        this.userHandlersClasses = handlersClasses;
    }

    public void initialize(){
        assert client != null;

        this.resourceFactories.add(ResourceFactory.ofInstance(this, SessionsManager.class));
        var splitHandlers = userHandlersClasses.stream()
                .collect(Collectors.<Class<? extends UpdateHandler>>partitioningBy(h -> h.getAnnotation(Handler.class).independent()));
        this.userHandlersClasses = splitHandlers.get(false);

        BotSender botSender = new BotSender(client, null);
        TranscriptionManager objectManager =  new TranscriptionManager(null, null,
                null, null, transcription, this::getTranscriptionManager,
                u -> new ResourcePool(u, null, botSender, null));

        this.handlersController = new UpdateHandlersController(new ResourceInjector(resourceFactories, client, botSender, objectManager));
        splitHandlers.get(true).forEach(this.handlersController::add);

        if(this.transcription.getRoot().getChatTypes() == null)
            this.transcription.getRoot().setChatTypes(new String[]{ChatTypes.ALL});

        if (Boolean.parseBoolean(this.transcription.getAutoCommands())) {
            try {
                this.client.execute(new DeleteMyCommands());
            } catch (TelegramApiException e) { throw new RuntimeException(e); }

            ChatTypes.GENERAL_SCOPES_LIST.forEach(l -> {
                SetMyCommands setMyCommands = this.transcription.getSetCommands(l);

                try {
                    if (!setMyCommands.getCommands().isEmpty())
                        this.client.execute(setMyCommands);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        if (transcription.getUsername() == null){
            try {
                transcription.setUsername(GeneratedValue.ofValue(botSender.execute(new GetMe()).getUserName()));
            } catch (TelegramApiException e) {
                throw new TelegRiseRuntimeException("Bot username wasn't specified and the attempt of getting one caused TelegramApiException: " + e.getMessage());
            }
        }
    }

    public void initializeSessions() {
        this.sessionInitializer.getInitializionList().forEach(this::createSession);
    }

    public void onUpdateReceived(Update update){
        log.debug("Update received: {}", update);
        Optional<UpdateHandler> candidate = this.handlersController.getApplicableHandler(update);
        if (candidate.isPresent()){
            this.handlersController.applyHandler(update, candidate.get());
            if (candidate.get().getClass().getAnnotation(Handler.class).absolute())
                return;
        }

        User from = MessageUtils.getFrom(update);
        if (from != null && !from.getIsBot()) {
            SessionIdentifier identifier;
            if (transcription.getSessionType().equals(SessionIdentifier.SESSION_CHAT)){
                Chat chat = MessageUtils.getChat(update);
                identifier = chat != null ? SessionIdentifier.of(from, chat) : SessionIdentifier.ofUserOnly(from);
            } else if (transcription.getSessionType().equals(SessionIdentifier.SESSION_USER)) {
                identifier = SessionIdentifier.ofUserOnly(from);
            } else
                throw new IllegalStateException(transcription.getSessionType());

            if (!sessions.containsKey(identifier))
                this.createSession(identifier);

            this.updateSession(this.sessions.get(identifier), update);
        }
    }

    @Override
    public void loadSession(SessionMemory memory){
        if (!(memory instanceof SessionMemoryImpl sessionMemory))
            throw new TelegRiseRuntimeException("Unable to load session with third-party implementation");

        if (this.sessions.containsKey(memory.getSessionIdentifier())) {
            log.warn("Unable to load session {}: session with the same credentials already exists", sessionMemory.getSessionIdentifier());
            return;
        }

        UserSession session = new UserSession(sessionMemory.getSessionIdentifier(), sessionMemory, transcription, client, this::getTranscriptionManager);

        this.sessions.put(sessionMemory.getSessionIdentifier(), session);
    }

    @Override
    public void createSession(SessionIdentifier identifier) {
        UserSession session = new UserSession(identifier, this.transcription, this.client, this::getTranscriptionManager);
        session.setStandardLanguage(identifier.getLanguageCode());
        session.getResourceInjector().addFactories(resourceFactories);
        session.setRoleProvider(this.roleProvider);
        session.addHandlersClasses(this.userHandlersClasses);

        if (this.sessionInitializer != null){
            this.sessionInitializer.initialize(session.getSessionMemory());
        }

        this.sessions.put(identifier, session);
    }

    @Override
    public void reinitializeSession(SessionIdentifier sessionIdentifier) {
        sessions.remove(sessionIdentifier);
        createSession(sessionIdentifier);
    }

    @Override
    public void killSession(SessionIdentifier identifier) {
        this.sessions.remove(identifier);
    }

    @Override
    public @Nullable SessionMemory getSessionMemory(SessionIdentifier sessionIdentifier) {
        return Optional.ofNullable(this.sessions.get(sessionIdentifier)).map(UserSession::getSessionMemory).orElse(null);
    }

    private void updateSession(UserSession session, Update update){
        session.update(update);

        if (!session.isRunning())
            session.run();
    }

    public TranscriptionManager getTranscriptionManager(SessionIdentifier identifier){
        UserSession session = this.sessions.get(identifier);
        return session == null ? null : session.getTranscriptionManager();
    }

}
