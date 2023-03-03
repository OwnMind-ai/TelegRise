package org.telegram.telegrise.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Update;

@Data @AllArgsConstructor @NoArgsConstructor
public final class ResourcePool {
    private Update update;
    private Object handler;

    public ResourcePool(Update update) {
        this.update = update;
    }
}