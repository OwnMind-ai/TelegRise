package org.telegram.telegrise;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrise.annotations.OnCreate;
import org.telegram.telegrise.annotations.Reference;
import org.telegram.telegrise.annotations.Resource;
import org.telegram.telegrise.annotations.TreeController;
import org.telegram.telegrise.senders.BotSender;

import java.io.File;

@SuppressWarnings("unused")
@TreeController
public class SimpleController {
    public static void main(String[] args) {
        TelegRiseApplication application = new TelegRiseApplication(new File("samples/index.xml"), SimpleController.class);

        application.start();
    }

    @Resource  // Injects resource into created instance, customizable
    private SessionMemory memory;

    @Resource
    private BotSender sender;

    @OnCreate
    public void initialize() {
        System.out.println("Someone pressed '/start'");
        try {
            sender.execute(SendMessage.builder().chatId(memory.getChatId()).text("text").build());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Reference  // Indicates that method can't be referenced at transcription by using '#' sign
    public void logResponse(Update update) {
        this.memory.put("response", update);
    }

    @Reference
    public boolean messageSent(Update update) {
        return update.hasMessage();
    }

    @Reference
    public boolean messageText(Update update, String text){
        return text.equals(update.getMessage().getText());
    }

    public Object getNull(){
        return null;
    }
}