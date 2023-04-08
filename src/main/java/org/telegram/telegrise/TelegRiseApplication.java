package org.telegram.telegrise;

import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrise.core.ApplicationNamespace;
import org.telegram.telegrise.core.LocalNamespace;
import org.telegram.telegrise.core.parser.XMLElementsParser;
import org.telegram.telegrise.core.parser.XMLTranscriptionParser;
import org.telegram.telegrise.core.utils.XMLUtils;
import org.telegram.telegrise.resources.ResourceFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

//TODO webhooks
//TODO multiple bots
//TODO bot options
public final class TelegRiseApplication {
    @Setter
    private File transcription;
    @Setter
    private ClassLoader classLoader = this.getClass().getClassLoader();
    private final List<Class<? extends PrimaryHandler>> handlersClasses = new ArrayList<>();
    private final List<ResourceFactory<?>> resourceFactories = new ArrayList<>();

    @Setter
    private RoleProvider roleProvider;

    private final TelegramBotsApi api;

    {
        try {
            api = new TelegramBotsApi(DefaultBotSession.class);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public TelegRiseApplication() {}

    public TelegRiseApplication(File transcription)  {
        this.transcription = transcription;
    }

    public void start(){
        TelegramSessionsController controller = this.createController();

        TelegramLongPollingBot bot = BotFactory.createLongPooling(controller.getTranscription(), controller::onUpdateReceived);
        controller.setSender(bot);
        controller.initialize();

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
            controller = new TelegramSessionsController(parser.parse(), this.roleProvider, resourceFactories, this.handlersClasses);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return controller;
    }

    public void addHandler(Class<? extends PrimaryHandler> handlerClass){
        this.handlersClasses.add(handlerClass);
    }

    public void addResourceFactory(ResourceFactory<?> factory){
        this.resourceFactories.add(factory);
    }
}
