package org.telegrise.telegrise.bot.handlers;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegrise.telegrise.UpdateHandler;
import org.telegrise.telegrise.annotations.Handler;
import org.telegrise.telegrise.annotations.Resource;
import org.telegrise.telegrise.keyboard.KeyboardBuilder;
import org.telegrise.telegrise.senders.BotSender;

import static org.telegrise.telegrise.keyboard.KeyboardBuilder.row;

@Handler(priority = 10)
public class MainTestHandler implements UpdateHandler {
    @Resource
    private BotSender sender;

    @Override
    public boolean canHandle(Update update) {
        return update.hasMessage() && "Test handlers".equals(update.getMessage().getText());
    }

    @Override
    public void handle(Update update) throws TelegramApiException {
        sender.of(update.getMessage()).send(
                "Choose handler to test or press /start to return.",
                KeyboardBuilder.replyBuilder(
                        row("Counter", "Global counter"),
                        row("Test exception handling")
                ).resizeKeyboard(true).build()
        );
    }
}
