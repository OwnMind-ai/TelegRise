package org.telegram.telegrise;

import lombok.Setter;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrise.core.ApplicationNamespace;
import org.telegram.telegrise.core.LocalNamespace;
import org.telegram.telegrise.core.parser.XMLElementsParser;
import org.telegram.telegrise.core.parser.XMLTranscriptionParser;
import org.telegram.telegrise.core.utils.XMLUtils;

import java.io.File;

//TODO webhooks
//TODO multiple bots
//TODO bot options
public final class TelegRiseApplication {
    @Setter
    private File transcription;
    @Setter
    private ClassLoader classLoader = this.getClass().getClassLoader();
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
        //FIXME
        XMLElementsParser elementsParser = new XMLElementsParser(new LocalNamespace(null, new ApplicationNamespace(classLoader)), transcription.getParentFile());
        elementsParser.load();

        TelegramSessionsController controller;
        try {
            XMLTranscriptionParser parser = new XMLTranscriptionParser(XMLUtils.loadDocument(transcription), elementsParser, classLoader);
            controller = new TelegramSessionsController(parser.parse());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        TelegramLongPollingBot bot = BotFactory.createLongPooling(controller.getTranscription(), controller::onUpdateReceived);
        controller.setSender(bot);

        try {
            api.registerBot(bot);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
