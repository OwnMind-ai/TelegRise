package org.telegram.telegrise.exceptions;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class TelegramApiRuntimeException extends RuntimeException{
    public TelegramApiRuntimeException(TelegramApiException e){
        super(e);
    }
}
