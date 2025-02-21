package org.telegrise.telegrise.core;

import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@ApiStatus.Internal
public class ResourceProvider {
    private final Map<String, Function<Class<?>, Object>> resourceProvider = new HashMap<>();

    public void add(Class<?> tClass, Function<Class<?>, Object> provider){
        resourceProvider.put(tClass.getName(), provider);
    }

    public void add(Class<?> tClass, Object resource){
        resourceProvider.put(tClass.getName(), c -> resource);
    }

    public <T> T get(Class<T> tClass, Class<?> targetClass){
        return tClass.cast(resourceProvider.getOrDefault(tClass.getName(), o -> null).apply(targetClass));
    }
}
