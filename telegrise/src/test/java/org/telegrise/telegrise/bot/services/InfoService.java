package org.telegrise.telegrise.bot.services;

import lombok.extern.slf4j.Slf4j;
import org.telegrise.telegrise.Service;
import org.telegrise.telegrise.annotations.Resource;
import org.telegrise.telegrise.types.BotUser;

@Slf4j
public class InfoService implements Service {
    @Resource
    private BotUser botUser;

    @Override
    public void run() {
        log.info("Bot: id={}, name={}", botUser.getId(), botUser.getUsername());
    }
}
