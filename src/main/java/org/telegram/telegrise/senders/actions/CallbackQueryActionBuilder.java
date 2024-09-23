package org.telegram.telegrise.senders.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.InaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrise.SessionMemoryImpl;
import org.telegram.telegrise.exceptions.TelegramApiRuntimeException;
import org.telegram.telegrise.senders.BotSender;

@SuppressWarnings("unused")
public class CallbackQueryActionBuilder{
    private static final Logger LOGGER = LoggerFactory.getLogger(CallbackQueryActionBuilder.class);
    private final CallbackQuery query;
    public final BotSender sender;
    private SessionMemoryImpl sessionMemory;

    public CallbackQueryActionBuilder(BotSender sender, CallbackQuery query, SessionMemoryImpl sessionMemory) {
        this.query = query;
        this.sender = sender;
    }

    public Message getMessage(){
        return (Message) query.getMessage();
    }

    public boolean isInaccessible(){
        return query.getMessage() instanceof InaccessibleMessage;
    }

    public boolean answer(String text) throws TelegramApiException{
        return this.answer(text, false);
    }

    public boolean answer(String text, boolean showAlert) throws TelegramApiException{
        return this.sender.execute(
                AnswerCallbackQuery.builder().callbackQueryId(query.getId()).showAlert(showAlert).text(text).build());
    }

    public EditableMessageActionBuilder message(){
        if (isInaccessible())
            throw new IllegalStateException("Message is inaccessible");

        return new EditableMessageActionBuilder(sender, getMessage(), sessionMemory);
    }
}
