package org.telegrise.telegrise;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.telegram.telegrambots.meta.api.methods.GetMe;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegrise.telegrise.annotations.Handler;
import org.telegrise.telegrise.application.ApplicationRunner;
import org.telegrise.telegrise.core.ResourceInjector;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.ServiceManager;
import org.telegrise.telegrise.core.TelegramSessionsController;
import org.telegrise.telegrise.core.parser.ApplicationNamespace;
import org.telegrise.telegrise.core.parser.LocalNamespace;
import org.telegrise.telegrise.core.parser.XMLElementsParser;
import org.telegrise.telegrise.core.parser.XMLTranscriptionParser;
import org.telegrise.telegrise.core.utils.XMLUtils;
import org.telegrise.telegrise.exceptions.TelegRiseInternalException;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegrise.telegrise.exceptions.TranscriptionParsingException;
import org.telegrise.telegrise.resources.ResourceFactory;
import org.telegrise.telegrise.senders.BotSender;
import org.telegrise.telegrise.types.BotUser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
public final class TelegRiseApplication {
    @Setter
    private File transcription;
    @Setter
    private ClassLoader classLoader = this.getClass().getClassLoader();
    private List<Class<? extends UpdateHandler>> handlersClasses = new ArrayList<>();
    private final List<ResourceFactory<?>> resourceFactories = new ArrayList<>();
    private final ServiceManager serviceManager = new ServiceManager();
    @Getter
    private final Class<?> mainClass;
    @Setter
    private Supplier<? extends ExecutorService> executorService;

    @Setter
    private RoleProvider roleProvider;
    @Setter
    private SessionInitializer sessionInitializer;
    @Setter
    private ApplicationRunner applicationRunner;
    private TelegramSessionsController sessionsController;
    private TelegramClient client;
    private String token;

    public TelegRiseApplication(Class<?> mainClass) {
        this.mainClass = mainClass;
    }

    public TelegRiseApplication(File transcription, Class<?> mainClass)  {
        this.transcription = transcription;
        this.mainClass = mainClass;
    }

    public void preload(){
        log.info("Starting TelegRise application...");
        this.handlersClasses = this.loadUpdateHandlers();
        this.sessionsController = this.createController();
        token = sessionsController.getTranscription().getToken().generate(new ResourcePool());

        if (token == null || !token.matches("\\d+:[a-zA-Z0-9_-]{35}"))
            throw new TelegRiseRuntimeException("Invalid bot token: " + token);

        client = sessionsController.getTranscription().produceClient();
        sessionsController.setClient(client);

        try {
            sessionsController.setBotUser(new BotUser(client.execute(new GetMe())));
        } catch (TelegramApiException e) {
            throw new TelegRiseRuntimeException("Attempt of getting bot's User caused TelegramApiException: " + e.getMessage());
        }
    }

    public void start(){
        if (sessionsController == null)
            preload();

        sessionsController.initialize();
        ResourceInjector resourceInjector = new ResourceInjector(this.resourceFactories, client, new BotSender(client, null), getBotUser());

        serviceManager.setInjector(resourceInjector);

        if (this.roleProvider != null) {
            sessionsController.setRoleProvider(this.roleProvider);
            resourceInjector.injectResources(this.roleProvider);
        }
        if (this.sessionInitializer != null) {
            sessionsController.setSessionInitializer(this.sessionInitializer);
            resourceInjector.injectResources(this.sessionInitializer);
            sessionsController.initializeSessions();
        }

        if(applicationRunner == null)
            this.applicationRunner = sessionsController.getTranscription().isWebhookBot() ?
                    ApplicationRunner.getWebhookRunner() : ApplicationRunner.LONG_POLLING;

        log.info("Starting bot server...");
        serviceManager.startServices();

        try {
            this.applicationRunner.run(sessionsController::onUpdateReceived, token, sessionsController.getTranscription(),
                    executorService == null ? null : executorService.get());
        } finally {
            serviceManager.stop();
        }
    }

    @NotNull
    private TelegramSessionsController createController() {
        ApplicationNamespace applicationNamespace = new ApplicationNamespace(classLoader, this.mainClass.getPackageName());
        XMLElementsParser elementsParser = new XMLElementsParser(new LocalNamespace(null, applicationNamespace), transcription.getParentFile());
        elementsParser.load();
        elementsParser.getTranscriptionMemory().getLinkedFiles().add(transcription); // to prevent cyclic imports

        TelegramSessionsController controller;
        try {
            XMLTranscriptionParser parser = new XMLTranscriptionParser(XMLUtils.loadDocument(transcription), elementsParser);
            controller = new TelegramSessionsController(parser.parse(), resourceFactories, this.handlersClasses);
        } catch (TelegRiseRuntimeException | TelegRiseInternalException | TranscriptionParsingException e) {
            throw TelegRiseRuntimeException.unfold(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return controller;
    }

    private List<Class<? extends UpdateHandler>> loadUpdateHandlers(){
        Set<Class<?>> handlerCandidates = new Reflections(this.mainClass.getPackageName()).getTypesAnnotatedWith(Handler.class);

        for (Class<?> clazz : handlerCandidates) {
            if (!UpdateHandler.class.isAssignableFrom(clazz))
                throw new TelegRiseRuntimeException("Handler class '" + clazz.getName() + "' doesn't implement PrimaryHandler interface");

            var annotation = clazz.getAnnotation(Handler.class);
            if (annotation.afterTrees() && annotation.independent())
                throw new TelegRiseRuntimeException("An independent handler '%s' can't be ran after trees".formatted(clazz.getSimpleName()));
        }

        //noinspection unchecked
        return handlerCandidates.stream().map(c -> (Class<? extends UpdateHandler>)c).collect(Collectors.toList());
    }

    @SuppressWarnings("unused")
    public void addResourceFactory(ResourceFactory<?> factory){
        this.resourceFactories.add(factory);
    }

    @SuppressWarnings("unused")
    public void addService(Service service){
        this.serviceManager.add(service);
    }

    public SessionsManager getSessionManager() {
        return sessionsController;
    }

    public TelegramClient getTelegramClient() {
        return client;
    }

    public BotUser getBotUser() {
        return this.sessionsController.getBotUser();
    }
}
