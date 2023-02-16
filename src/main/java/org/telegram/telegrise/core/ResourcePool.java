package org.telegram.telegrise.core;

import java.util.Map;

public final class ResourcePool {
    private final Map<String, Object> resources;

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
