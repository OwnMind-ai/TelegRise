package org.telegrise.telegrise.bot.handlers;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegrise.telegrise.UpdateHandler;
import org.telegrise.telegrise.annotations.Handler;
import org.telegrise.telegrise.annotations.Resource;
import org.telegrise.telegrise.senders.BotSender;

@Handler(afterTrees = true)
public class UnrecognizedCommandHandler implements UpdateHandler {
    @Resource
    private BotSender sender;

    @Override
    public boolean canHandle(Update update) {
        return update.hasMessage() && update.getMessage().hasText() && update.getMessage().getText().startsWith("/");
    }

    @Override
    public void handle(Update update) throws TelegramApiException {
        sender.of(update.getMessage()).send("Unrecognized command. Say what?");
    }
}
