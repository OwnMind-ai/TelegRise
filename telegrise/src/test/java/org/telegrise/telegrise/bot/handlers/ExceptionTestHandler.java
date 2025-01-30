package org.telegrise.telegrise.bot.handlers;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegrise.telegrise.SessionMemory;
import org.telegrise.telegrise.UpdateHandler;
import org.telegrise.telegrise.annotations.Handler;
import org.telegrise.telegrise.annotations.Resource;
import org.telegrise.telegrise.senders.BotSender;

@Handler
public class ExceptionTestHandler implements UpdateHandler {
    @Resource
    private SessionMemory memory;
    @Resource
    private BotSender sender;

    @Override
    public boolean canHandle(Update update) {
        return update.hasMessage() && "Test exception handling".equals(update.getMessage().getText());
    }

    @Override
    public void handle(Update update) throws TelegramApiException {
        throw new TelegramApiException();
    }

    @Override
    public void onException(TelegramApiException e) {
        try {
            sender.of(memory.getChatId()).send("Exception caught successfully");
        } catch (TelegramApiException ex) {
            throw new RuntimeException(ex);
        }
    }
}
