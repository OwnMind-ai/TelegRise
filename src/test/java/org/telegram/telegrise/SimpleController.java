package org.telegram.telegrise;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrise.annotations.OnCreate;
import org.telegram.telegrise.annotations.Reference;
import org.telegram.telegrise.annotations.Resource;
import org.telegram.telegrise.annotations.TreeController;
import org.telegram.telegrise.caching.CachingStrategy;
import org.telegram.telegrise.senders.BotSender;

import java.io.File;
import java.util.List;

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
    }

    @Reference
    public void respond(Update update) throws InterruptedException {
        Message result = sender.of(update.getMessage())
                .replyMarkup(new InlineKeyboardMarkup(List.of(new InlineKeyboardRow(InlineKeyboardButton.builder().text("a").callbackData("a").build()))))
                .disableNotification(true)
                .disableWebPagePreview(true)
                .reply("response");

        Thread.sleep(1000);

        sender.ofEditable(result).edit("edited", InlineKeyboardMarkup.builder().clearKeyboard().build());
    }

    @Reference  // Indicates that method can be referenced at transcription by using '#' sign
    public void logResponse(Update update) {
        this.memory.put("response", update);
        sender.of(update.getCallbackQuery()).answer("Ok");
    }

    @Reference
    public boolean messageSent(Update update) {
        return update.hasMessage();
    }

    @Reference
    public boolean messageText(Update update, String text){
        return text.equals(update.getMessage().getText());
    }

    @Reference(caching = CachingStrategy.UPDATE)
    public Integer extractNumber(Update update){
        try {
            return Integer.parseInt(update.getMessage().getText());
        } catch (NumberFormatException e){
            return null;
        }
    }

    @Reference
    public boolean notNull(Object o){
        return o != null;
    }

    @Reference
    public void process(Integer i){
        System.out.println(i);
    }

    @Reference
    public String getNull(){
        return null;
    }
}