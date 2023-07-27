package org.telegram.telegrise.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ClassUtils;
import org.jetbrains.annotations.Contract;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrise.SessionMemory;
import org.telegram.telegrise.SessionMemoryImpl;
import org.telegram.telegrise.TreeExecutor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor @AllArgsConstructor
public final class ResourcePool {
    public static Object extractComponent(Map<Class<?>, Object> components, Class<?> target){
        if (components.containsKey(target)) return components.get(target);

        return components.keySet().stream().filter(k -> ClassUtils.isAssignable(k, target))
                .map(components::get)
                .findFirst().orElse(null);
    }

    private Update update;
    private Object handler;

    private DefaultAbsSender sender;

    private SessionMemoryImpl memory;

    private TreeExecutor currentExecutor;

    private final Map<Class<?>, Object> components = new HashMap<>();

    public ResourcePool(Update update, Object handler, DefaultAbsSender sender, SessionMemoryImpl memory) {
        this.update = update;
        this.handler = handler;
        this.sender = sender;
        this.memory = memory;
    }

    @Contract("_ -> this")
    public ResourcePool addComponent(Object object){
        this.components.put(object.getClass(), object);
        return this;
    }

    public Map<Class<?>, Object> getComponents(){
        if (update != null) this.addComponent(update);
        if (sender != null) this.addComponent(sender);
        if (memory != null) this.components.put(SessionMemory.class, memory);

        return this.components;
    }
}