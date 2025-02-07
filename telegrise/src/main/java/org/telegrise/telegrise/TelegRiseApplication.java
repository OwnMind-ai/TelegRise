package org.telegrise.telegrise;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
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
    private final Class<?> mainClass;
    @Setter
    private Supplier<? extends ExecutorService> executorService;

    @Setter
    private RoleProvider roleProvider;
    @Setter
    private SessionInitializer sessionInitializer;
    @Setter
    private ApplicationRunner applicationRunner;

    public TelegRiseApplication(Class<?> mainClass) {
        this.mainClass = mainClass;
    }

    public TelegRiseApplication(File transcription, Class<?> mainClass)  {
        this.transcription = transcription;
        this.mainClass = mainClass;
    }

    public void start(){
        log.info("Starting TelegRise application...");
        this.handlersClasses = this.loadPrimaryHandlers();
        TelegramSessionsController controller = this.createController();
        final String token = controller.getTranscription().getToken().generate(new ResourcePool());

        if (token == null || !token.matches("\\d+:[a-zA-Z0-9_-]{35}"))
            throw new TelegRiseRuntimeException("Invalid bot token: " + token);

        TelegramClient client = controller.getTranscription().produceClient();
        controller.setClient(client);
        controller.initialize();

        ResourceInjector resourceInjector = new ResourceInjector(this.resourceFactories, client, new BotSender(client, null));

        serviceManager.setInjector(resourceInjector);
        serviceManager.startServices();

        if (this.sessionInitializer != null) {
            resourceInjector.injectResources(this.sessionInitializer);
            controller.initializeSessions();
        }

        if(applicationRunner == null)
            this.applicationRunner = controller.getTranscription().isWebhookBot() ?
                    ApplicationRunner.WEBHOOK : ApplicationRunner.LONG_POLLING;

        log.info("Starting bot server...");

        try {
            this.applicationRunner.run(controller::onUpdateReceived, token, controller.getTranscription(),
                    executorService == null ? null : executorService.get());
        } finally {
            serviceManager.stop();
        }
    }

    @NotNull
    private TelegramSessionsController createController() {
        XMLElementsParser elementsParser = new XMLElementsParser(new LocalNamespace(null, new ApplicationNamespace(classLoader)), transcription.getParentFile());
        elementsParser.load();
        elementsParser.getTranscriptionMemory().getLinkedFiles().add(transcription); // to prevent cyclic imports

        TelegramSessionsController controller;
        try {
            XMLTranscriptionParser parser = new XMLTranscriptionParser(XMLUtils.loadDocument(transcription), elementsParser, classLoader);
            controller = new TelegramSessionsController(parser.parse(), resourceFactories, this.handlersClasses);
            controller.setRoleProvider(this.roleProvider);
            controller.setSessionInitializer(this.sessionInitializer);
        } catch (TelegRiseRuntimeException | TelegRiseInternalException | TranscriptionParsingException e) {
            throw TelegRiseRuntimeException.unfold(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return controller;
    }

    private List<Class<? extends UpdateHandler>> loadPrimaryHandlers(){
        Set<Class<?>> handlerCandidates = new Reflections(this.mainClass.getPackageName()).getTypesAnnotatedWith(Handler.class);

        for (Class<?> clazz : handlerCandidates)
            if (!UpdateHandler.class.isAssignableFrom(clazz))
                throw new TelegRiseRuntimeException("Handler class '" + clazz.getName() + "' doesn't implement PrimaryHandler interface");

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
}
