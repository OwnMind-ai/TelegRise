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
import org.telegram.telegrise.annotations.Handler;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.BotTranscription;
import org.telegram.telegrise.resources.ResourceFactory;
import org.telegram.telegrise.resources.ResourceInjector;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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
    private List<Class<? extends PrimaryHandler>> userHandlersClasses;
    private PrimaryHandlersController handlersController;

    public TelegramSessionsController(BotTranscription transcription, RoleProvider roleProvider, List<ResourceFactory<?>> resourceFactories, List<Class<? extends PrimaryHandler>> handlersClasses) {
        this.transcription = transcription;
        this.roleProvider = roleProvider;
        this.resourceFactories = resourceFactories;
        this.poolExecutor = this.createExecutorService();
        this.handlersController = new PrimaryHandlersController(null);
        this.userHandlersClasses = handlersClasses;
    }

    private ExecutorService createExecutorService(){
        String property = System.getProperty("telegrise.threadPoolSize");

        if(NumberUtils.isDigits(property))
            return Executors.newFixedThreadPool(Integer.parseInt(property));

        return Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
    }

    public void initialize(){
        assert sender != null;

        var splitHandlers = userHandlersClasses.stream()
                .collect(Collectors.<Class<? extends PrimaryHandler>>partitioningBy(h -> h.getAnnotation(Handler.class).independent()));
        this.userHandlersClasses = splitHandlers.get(false);

        InteractiveObjectManager objectManager =  new InteractiveObjectManager(u -> new ResourcePool(u, null, sender, null));
        objectManager.load(transcription);

        this.handlersController = new PrimaryHandlersController(new ResourceInjector(resourceFactories, sender, objectManager));
        splitHandlers.get(true).forEach(this.handlersController::add);

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
        Optional<PrimaryHandler> candidate = this.handlersController.getApplicableHandler(update);
        if (candidate.isPresent()){
            boolean intercept = this.handlersController.applyHandler(update, candidate.get());

            if (intercept) return;
        }

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
        session.addHandlersClasses(this.userHandlersClasses);
        this.sessions.put(identifier, session);
    }

    private void updateSession(UserSession session, Update update){
        session.update(update);

        if (!session.isRunning())
            poolExecutor.submit(() -> {
                try{
                    session.run();
                } catch (Exception e){
                    System.err.println(e.getMessage());  //TODO fix exceptions
                }
            });
    }
}
