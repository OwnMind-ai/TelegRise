package org.telegram.telegrise.core;

import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;

public final class ResourcePool {
    private final Map<String, Object> resources;
    @Getter @Setter
    private Update update;

    public ResourcePool(Update update){
        this.resources = new HashMap<>();
        this.update = update;
    }

    public ResourcePool(Map<String, Object> resources) {
        this.resources = resources;
    }

    public void set(String name, Object object){
        this.resources.put(name, object);
    }

    public Object get(String name){
        return this.resources.get(name);
    }

    public <T> T get(String name, Class<T> clazz){
        return clazz.cast(this.resources.get(name));
    }
}
