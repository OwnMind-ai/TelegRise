package org.telegram.telegrise.core;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrise.SessionMemory;

@Data
@NoArgsConstructor
public final class ResourcePool {
    private Update update;
    private Object handler;

    private DefaultAbsSender sender;

    private SessionMemory memory;

    public ResourcePool(Update update, Object handler, DefaultAbsSender sender, SessionMemory memory) {
        this.update = update;
        this.handler = handler;
        this.sender = sender;
        this.memory = memory;
    }
}