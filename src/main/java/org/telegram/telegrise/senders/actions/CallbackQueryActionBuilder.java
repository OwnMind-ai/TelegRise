package org.telegram.telegrise.senders.actions;

import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.InaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrise.exceptions.TelegramApiRuntimeException;
import org.telegram.telegrise.senders.BotSender;

@SuppressWarnings("unused")
public class CallbackQueryActionBuilder{
    private final CallbackQuery query;
    public final BotSender sender;

    public CallbackQueryActionBuilder(BotSender sender, CallbackQuery query) {
        this.query = query;
        this.sender = sender;
    }

    public Message getMessage(){
        return (Message) query.getMessage();
    }

    public boolean isInaccessible(){
        return query.getMessage() instanceof InaccessibleMessage;
    }

    public boolean answer(String text){
        return this.answer(text, false);
    }

    public boolean answer(String text, boolean showAlert){
        try {
            return this.sender.execute(AnswerCallbackQuery.builder()
                            .callbackQueryId(query.getId())
                            .showAlert(showAlert)
                            .text(text)
                    .build());
        } catch (TelegramApiException e) {
            throw new TelegramApiRuntimeException(e);
        }
    }

    public EditableMessageActionBuilder message(){
        if (isInaccessible())
            throw new IllegalStateException("Message is inaccessible");

        return new EditableMessageActionBuilder(sender, getMessage());
    }
}
