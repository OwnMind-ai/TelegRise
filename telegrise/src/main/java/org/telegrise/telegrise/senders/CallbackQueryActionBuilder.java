package org.telegrise.telegrise.senders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.InaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegrise.telegrise.core.SessionMemoryImpl;

/**
 * A builder for Telegram callback-related API methods.
 * <p>
 * This class extracts all required values from provided {@code CallbackQuery}.
 * Method {@code message} will return {@link EditableMessageActionBuilder}
 * for handling the message attached to the query.
 * <pre>
 * {@code
 * sender.of(query).answer("Hello, World!");
 * }
 * </pre>
 *
 * @see EditableMessageActionBuilder
 * @since 0.6
 */
@SuppressWarnings("unused")
public class CallbackQueryActionBuilder{
    private static final Logger LOGGER = LoggerFactory.getLogger(CallbackQueryActionBuilder.class);
    private final CallbackQuery query;
    public final BotSender sender;
    private SessionMemoryImpl sessionMemory;

    CallbackQueryActionBuilder(BotSender sender, CallbackQuery query, SessionMemoryImpl sessionMemory) {
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
        return this.sender.getClient().execute(
                AnswerCallbackQuery.builder().callbackQueryId(query.getId()).showAlert(showAlert).text(text).build());
    }

    public EditableMessageActionBuilder message(){
        if (isInaccessible())
            throw new IllegalStateException("Message is inaccessible");

        return new EditableMessageActionBuilder(sender, getMessage(), sessionMemory);
    }
}
