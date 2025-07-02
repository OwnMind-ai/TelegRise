package org.telegrise.telegrise.core;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
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
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegrise.telegrise.exceptions.TelegRiseSessionException;
import org.telegrise.telegrise.resources.ResourceFactory;
import org.telegrise.telegrise.senders.BotSender;
import org.telegrise.telegrise.types.BotUser;
import org.telegrise.telegrise.utils.ChatTypes;
import org.telegrise.telegrise.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
public class TelegramSessionsController implements SessionsManager, InternalSessionExtensions {
    private final ConcurrentMap<SessionIdentifier, UserSession> sessions = new ConcurrentHashMap<>();
    @Getter
    private final BotTranscription transcription;
    @Setter
    private RoleProvider roleProvider;
    @Setter
    private SessionInitializer sessionInitializer;
    private final List<ResourceFactory<?>> resourceFactories;
    private final List<BiConsumer<SessionIdentifier, SessionMemory>> destructionCallbacks = new ArrayList<>();

    @Setter
    private TelegramClient client;
    private List<Class<? extends UpdateHandler>> userHandlersClasses;
    private UpdateHandlersController handlersController;
    @Getter
    private TranscriptionManager transcriptionManager;
    @Getter @Setter
    private BotUser botUser;
    @Setter
    private ResourceInjector mainInjector;

    public TelegramSessionsController(BotTranscription transcription, List<ResourceFactory<?>> resourceFactories, List<Class<? extends UpdateHandler>> handlersClasses) {
        this.transcription = transcription;
        this.resourceFactories = resourceFactories;
        this.handlersController = new UpdateHandlersController(null);
        this.userHandlersClasses = handlersClasses;
    }

    public void initialize() {
        assert client != null;
        assert mainInjector != null;

        this.resourceFactories.add(ResourceFactory.ofInstance(this, SessionsManager.class));
        var splitHandlers = userHandlersClasses.stream()
                .collect(Collectors.<Class<? extends UpdateHandler>>partitioningBy(h -> h.getAnnotation(Handler.class).independent()));
        this.userHandlersClasses = splitHandlers.get(false);

        BotSender botSender = new BotSender(client, null);
        this.transcriptionManager = new TranscriptionManager(transcription, u -> new ResourcePool(u, null, botSender, null, botUser));

        this.handlersController = new UpdateHandlersController(mainInjector);
        splitHandlers.get(true).forEach(this.handlersController::add);

        if (this.transcription.getRoot().getChatTypes() == null)
            this.transcription.getRoot().setChatTypes(new String[]{ChatTypes.ALL});

        if (Boolean.parseBoolean(this.transcription.getAutoCommands())) {
            try {
                this.client.execute(new DeleteMyCommands());
            } catch (TelegramApiException e) {
                log.error("Unable to delete commands", e);
            }

            ChatTypes.GENERAL_SCOPES_LIST.forEach(l -> {
                SetMyCommands setMyCommands = this.transcription.getSetCommands(l);

                try {
                    if (!setMyCommands.getCommands().isEmpty())
                        this.client.execute(setMyCommands);
                } catch (TelegramApiException e) {
                    log.error("Unable to set commands", e);
                }
            });
        }
    }

    public void initializeSessions() {
        this.sessionInitializer.getInitializionList().forEach(identifier -> createSession(identifier, null));
    }

    public void onUpdateReceived(Update update){
        log.debug("Update received: {}", update);
        var candidates = this.handlersController.getApplicableHandlers(update);
        if (!candidates.isEmpty()){
            var absolute = this.handlersController.applyHandlers(update, candidates);
            if (absolute) return;
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
                this.createSession(identifier, from.getLanguageCode());

            this.updateSession(this.sessions.get(identifier), update);
        }
    }

    private void updateSession(UserSession session, Update update){
        session.update(update);

        if (!session.isRunning())
            session.run();
    }

    @Override
    public void loadSession(SessionMemory memory){
        if (!(memory instanceof SessionMemoryImpl sessionMemory))
            throw new TelegRiseRuntimeException("Unable to load session with third-party implementation");

        if (this.sessions.containsKey(memory.getSessionIdentifier())) {
            log.warn("Unable to load session {}: session with the same credentials already exists", sessionMemory.getSessionIdentifier());
            return;
        }

        UserSession session = new UserSession(sessionMemory.getSessionIdentifier(), sessionMemory, transcription);
        session.setStandardLanguage(memory.getLanguageCode());
        session.initialize(client, this.userHandlersClasses, mainInjector);

        this.sessions.put(sessionMemory.getSessionIdentifier(), session);
    }

    @Override
    public void createSession(SessionIdentifier identifier, @Nullable String languageCode) {
        UserSession session = new UserSession(identifier, this.transcription);
        this.sessions.put(identifier, session);  // This MUST happen before session#addHandlersClasses
        session.setStandardLanguage(languageCode);
        session.initialize(client, this.userHandlersClasses, mainInjector);

        if (this.sessionInitializer != null)
            this.sessionInitializer.initialize(session.getSessionMemory());
        if (this.roleProvider != null)
            session.getSessionMemory().setUserRole(this.roleProvider.getRole(session.getSessionMemory()));
    }

    @Override
    public void reinitializeSession(SessionIdentifier sessionIdentifier) {
        var session = sessions.remove(sessionIdentifier);
        createSession(sessionIdentifier, session.getSessionMemory().getLanguageCode());
    }

    @Override
    public void killSession(SessionIdentifier identifier) {
        var session = this.sessions.remove(identifier);
        destructionCallbacks.forEach(c -> c.accept(identifier, session.getSessionMemory()));
    }

    @Override
    public @Nullable SessionMemory getSessionMemory(SessionIdentifier sessionIdentifier) {
        return Optional.ofNullable(this.sessions.get(sessionIdentifier)).map(UserSession::getSessionMemory).orElse(null);
    }

    @Override
    public TranscriptionManager getTranscriptionManager(SessionIdentifier identifier){
        UserSession session = this.sessions.get(identifier);
        return session == null ? null : session.getTranscriptionManager();
    }

    @Override
    public void registerSessionDestructionCallback(BiConsumer<SessionIdentifier, SessionMemory> callback) {
        destructionCallbacks.add(callback);
    }

    @Override
    public boolean isSessionActive(SessionIdentifier identifier) {
        return this.sessions.containsKey(identifier);
    }

    @Override
    public <T> T runWithSessionContext(SessionIdentifier identifier, Supplier<T> runnable) {
        UserSession session = this.sessions.get(identifier);
        if (session == null)
            throw new TelegRiseSessionException("Session not found: " + identifier);

        TelegRiseSessionContext.setCurrentContext(identifier, session.getSessionMemory(), session.getResourceInjector());
        try {
            return runnable.get();
        } finally {
            TelegRiseSessionContext.clearContext();
        }
    }
}