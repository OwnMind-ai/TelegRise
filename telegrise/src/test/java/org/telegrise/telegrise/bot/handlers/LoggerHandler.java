package org.telegrise.telegrise.bot.handlers;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegrise.telegrise.UpdateHandler;
import org.telegrise.telegrise.annotations.Handler;

@Slf4j
@Handler(absolute = false)
public class LoggerHandler implements UpdateHandler {
    @Override
    public boolean canHandle(Update update) { return true; }

    @Override
    public void handle(Update update) {
        log.info("Received update id: {}", update.getUpdateId());
    }
}
