package org.telegrise.telegrise;

import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface PrimaryHandler {
    boolean canHandle(Update update);
    void handle(Update update) throws TelegramApiException;

    default void onException(TelegramApiException e){
        LoggerFactory.getLogger(this.getClass()).error("An exception occurred while executing handler", e);
    }
}
