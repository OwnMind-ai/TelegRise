package org.telegrise.telegrise.bot.handlers;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegrise.telegrise.UpdateHandler;
import org.telegrise.telegrise.annotations.Handler;
import org.telegrise.telegrise.annotations.Resource;
import org.telegrise.telegrise.senders.BotSender;

@Handler(independent = true)
public class GlobalCounterHandler implements UpdateHandler {
    @Resource
    private BotSender sender;

    private int value = 0;

    @Override
    public boolean canHandle(Update update) {
        return update.hasMessage() && "Global counter".equals(update.getMessage().getText());
    }

    @Override
    public void handle(Update update) throws TelegramApiException {
        sender.of(update.getMessage()).reply("Global counter: " + ++value);
    }
}
