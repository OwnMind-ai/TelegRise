package org.telegrise.telegrise.core;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ClassUtils;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegrise.telegrise.SessionMemory;
import org.telegrise.telegrise.core.utils.ReflectionUtils;
import org.telegrise.telegrise.senders.BotSender;
import org.telegrise.telegrise.types.ApiResponse;
import org.telegrise.telegrise.types.BotUser;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

//TODO refactor
// Resource pool must not be created arbitrarily, but using a factory that can have parent factory.
// This approach will resolve the mess of manually including every essential resource in every class that uses it.

@Data
@NoArgsConstructor
public final class ResourcePool {
    public static Object extractComponent(Map<Class<?>, Object> components, Class<?> target){
        if (components.containsKey(target)) return components.get(target);

        for (Class<?> k : components.keySet())
            if (ClassUtils.isAssignable(k, target))
                return components.get(k);

        return null;
    }

    private Update update;
    private Object handler;

    private BotSender sender;

    private SessionMemoryImpl memory;

    private TreeExecutor currentExecutor;
    private BlockingQueue<Update> updates;

    private BotUser botUser;
    private ApiResponse apiResponse;

    @Getter
    private final Map<Class<?>, Object> components = new HashMap<>(Map.of(ResourcePool.class,this));

    public ResourcePool(Update update, Object handler, BotSender sender, SessionMemoryImpl memory, BotUser botUser) {
        this.update = update;
        this.handler = handler;
        this.sender = sender;
        this.memory = memory;
        this.botUser = botUser;
        bakeComponents();
    }

    public ResourcePool(Update update, Object handler, BotSender sender, SessionMemoryImpl memory, BotUser botUser,
                        TreeExecutor executor, BlockingQueue<Update> updates) {
        this.update = update;
        this.handler = handler;
        this.sender = sender;
        this.memory = memory;
        this.botUser = botUser;
        this.currentExecutor = executor;
        this.updates = updates;
        bakeComponents();
    }

    public void addComponent(Object object){
        this.components.put(ReflectionUtils.getClass(object), object);
    }

    private void bakeComponents(){
        if (update != null) this.addComponent(update);
        if (sender != null) {
            this.addComponent(sender);
            this.components.put(TelegramClient.class, sender.getClient());
        }
        if (memory != null) this.components.put(SessionMemory.class, memory);
        if (apiResponse != null) this.components.put(ApiResponse.class, apiResponse);
    }

    @Override
    public String toString() {
        return "ResourcePool{" + "update=" + update + '}';
    }
}