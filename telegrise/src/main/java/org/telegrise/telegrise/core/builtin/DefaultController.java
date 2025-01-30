package org.telegrise.telegrise.core.builtin;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegrise.telegrise.annotations.Reference;
import org.telegrise.telegrise.annotations.ReferenceGenerator;
import org.telegrise.telegrise.annotations.TreeController;
import org.telegrise.telegrise.core.elements.NodeElement;
import org.telegrise.telegrise.generators.GeneratedReference;
import org.w3c.dom.Node;

@Slf4j
@TreeController
public class DefaultController {
    //TODO consider making Update a @HiddenParameter, might prove useful
    @Reference
    public void logError(TelegramApiException e, Node node){
        log.error("An error occurred when executing: {}", NodeElement.formatNode(node), e);
    }

    @Reference
    public void ignore(){}

    @Reference
    public String concat(Object... parts){
        StringBuilder sb = new StringBuilder();
        for (Object part : parts) {
            String string = part.toString();
            sb.append(string);
        }
        return sb.toString();
    }

    @Reference
    public boolean isCallback(Update update){
        return update.hasCallbackQuery();
    }

    @ReferenceGenerator
    public GeneratedReference<Update, Boolean> callback(String data){
        return u -> u.hasCallbackQuery() && u.getCallbackQuery().getData().equals(data);
    }

    @ReferenceGenerator
    public GeneratedReference<Update, Boolean> callbackMatches(String regex){
        return u -> u.hasCallbackQuery() && u.getCallbackQuery().getData().matches(regex);
    }

    @Reference
    public Integer getMessageId(Message message){
        return message == null ? null : message.getMessageId();
    }

    @Reference
    public boolean isMessage(Update update){
        return update.hasMessage();
    }

    @Reference
    public boolean isTextMessage(Update update){
        return update.hasMessage() && update.getMessage().hasText();
    }

    @Reference
    public boolean isCaptionMessage(Update update){
        return update.hasMessage() && update.getMessage().hasCaption();
    }

    @ReferenceGenerator
    public GeneratedReference<Update, Boolean> messageText(String text){
        return u -> u.hasMessage() && text.equals(u.getMessage().getText());
    }

    @ReferenceGenerator
    public GeneratedReference<Update, Boolean> messageTextMatches(String regex){
        return u -> u.hasMessage() && u.getMessage().hasText() && u.getMessage().getText().matches(regex);
    }

    @ReferenceGenerator
    public GeneratedReference<Update, Boolean> messageCaption(String text){
        return u -> u.hasMessage() && text.equals(u.getMessage().getCaption());
    }

    @ReferenceGenerator
    public GeneratedReference<Update, Boolean> messageCaptionMatches(String regex){
        return u -> u.hasMessage() && u.getMessage().hasCaption() && u.getMessage().getCaption().matches(regex);
    }

    @ReferenceGenerator
    public GeneratedReference<Update, Boolean> messageTextOrCaption(String text){
        return u -> u.hasMessage() && (text.equals(u.getMessage().getText()) || text.equals(u.getMessage().getCaption()));
    }

    @ReferenceGenerator
    public GeneratedReference<Update, Boolean> messageTextOrCaptionMatches(String regex){
        return u -> u.hasMessage() && ((u.getMessage().hasText() && u.getMessage().getText().matches(regex))
                || (u.getMessage().hasCaption() && u.getMessage().getCaption().matches(regex)));
    }

    @Reference
    public static ReplyKeyboard replyKeyboardRemove(){
        return new ReplyKeyboardRemove(true);
    }
}
