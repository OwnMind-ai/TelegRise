package org.telegram.telegrise.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrise.SessionMemoryImpl;
import org.telegram.telegrise.TreeExecutor;

@Data
@NoArgsConstructor @AllArgsConstructor
public final class ResourcePool {
    private Update update;
    private Object handler;

    private DefaultAbsSender sender;

    private SessionMemoryImpl memory;

    private TreeExecutor currentExecutor;

    public ResourcePool(Update update, Object handler, DefaultAbsSender sender, SessionMemoryImpl memory) {
        this.update = update;
        this.handler = handler;
        this.sender = sender;
        this.memory = memory;
    }
}