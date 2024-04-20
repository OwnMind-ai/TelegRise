package org.telegram.telegrise;

import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrise.annotations.Handler;
import org.telegram.telegrise.core.ApplicationNamespace;
import org.telegram.telegrise.core.LocalNamespace;
import org.telegram.telegrise.core.parser.XMLElementsParser;
import org.telegram.telegrise.core.parser.XMLTranscriptionParser;
import org.telegram.telegrise.core.utils.XMLUtils;
import org.telegram.telegrise.resources.ResourceFactory;
import org.telegram.telegrise.resources.ResourceInjector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

//TODO webhooks
//TODO multiple bots
//TODO bot options
public final class TelegRiseApplication {
    @Setter
    private File transcription;
    @Setter
    private ClassLoader classLoader = this.getClass().getClassLoader();
    private List<Class<? extends PrimaryHandler>> handlersClasses = new ArrayList<>();
    private final List<ResourceFactory<?>> resourceFactories = new ArrayList<>();
    private final ServiceManager serviceManager = new ServiceManager();
    private final Class<?> mainClass;

    @Setter
    private RoleProvider roleProvider;
    @Setter
    private SessionInitializer sessionInitializer;

    private final TelegramBotsApi api;

    {
        try {
            api = new TelegramBotsApi(DefaultBotSession.class);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public TelegRiseApplication(Class<?> mainClass) {
        this.mainClass = mainClass;
    }

    public TelegRiseApplication(File transcription, Class<?> mainClass)  {
        this.transcription = transcription;
        this.mainClass = mainClass;
    }

    public void start(){
        this.handlersClasses = this.loadPrimaryHandlers();
        TelegramSessionsController controller = this.createController();

        TelegramLongPollingBot bot = BotFactory.createLongPooling(controller.getTranscription(), controller::onUpdateReceived);
        controller.setSender(bot);
        controller.initialize();

        ResourceInjector resourceInjector = new ResourceInjector(this.resourceFactories, bot);

        serviceManager.setInjector(resourceInjector);
        serviceManager.startServices();

        if (this.sessionInitializer != null) {
            resourceInjector.injectResources(this.sessionInitializer);
            controller.initializeSessions();
        }

        try {
            api.registerBot(bot);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private TelegramSessionsController createController() {
        XMLElementsParser elementsParser = new XMLElementsParser(new LocalNamespace(null, new ApplicationNamespace(classLoader)), transcription.getParentFile());
        elementsParser.load();

        TelegramSessionsController controller;
        try {
            XMLTranscriptionParser parser = new XMLTranscriptionParser(XMLUtils.loadDocument(transcription), elementsParser, classLoader);
            controller = new TelegramSessionsController(parser.parse(), resourceFactories, this.handlersClasses);
            controller.setRoleProvider(this.roleProvider);
            controller.setSessionInitializer(this.sessionInitializer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return controller;
    }

    private List<Class<? extends PrimaryHandler>> loadPrimaryHandlers(){
        Set<Class<?>> handlerCandidates = new Reflections(this.mainClass.getPackageName()).getTypesAnnotatedWith(Handler.class);

        for (Class<?> clazz : handlerCandidates)
            if (!PrimaryHandler.class.isAssignableFrom(clazz))
                throw new TelegRiseRuntimeException("Handler class '" + clazz.getName() + "' doesn't implement PrimaryHandler interface");

        return handlerCandidates.stream().map(c -> (Class<? extends PrimaryHandler>)c).collect(Collectors.toList());
    }

    public void addResourceFactory(ResourceFactory<?> factory){
        this.resourceFactories.add(factory);
    }

    public void addService(Service service){
        this.serviceManager.add(service);
    }
}
