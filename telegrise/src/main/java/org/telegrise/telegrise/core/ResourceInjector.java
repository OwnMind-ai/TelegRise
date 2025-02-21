package org.telegrise.telegrise.core;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;
import org.telegrise.telegrise.annotations.Resource;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegrise.telegrise.resources.ResourceFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public final class ResourceInjector {
    @Setter(onMethod_ = @ApiStatus.Internal)
    private static Function<Class<?>, Object> instanceInitializer = (c) -> {
        try {
            return c.getConstructor().newInstance();
        }  catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            String startMessage = "Cannot create instance of '" + c.getSimpleName() + "': ";

            if (e instanceof NoSuchMethodException)
                throw new TelegRiseRuntimeException(startMessage + "class must have constructor with no arguments");
            else
                throw new TelegRiseRuntimeException(startMessage + e.getMessage());
        }
    };

    @Getter
    private final Map<String, ResourceFactory<?>> resourceFactoryMap = new HashMap<>();
    private final List<Object> resources;
    /**
     * This is a <i>temporary</i> solution to the problem of bean's initialization for Spring support
     */
    @Getter @SuppressWarnings("WriteOnlyObject")
    private final AtomicReference<Class<?>> currentlyCreating = new AtomicReference<>(null);

    public ResourceInjector(Object... resources) {
        this.resources = List.of(resources);
    }

    public ResourceInjector(List<ResourceFactory<?>> resourceFactories, Object... resources) {
        this.resources = List.of(resources);
        this.addFactories(resourceFactories);
    }

    public void addFactories(List<ResourceFactory<?>> resourceFactories) {
        resourceFactories.forEach(f -> this.resourceFactoryMap.put(f.getResourceClass().getName(), f));
    }

    public void injectResources(Object target){
        for (Field field : getFieldsToInject(target.getClass())) {
            if (!field.isAnnotationPresent(Resource.class)) continue;
            field.setAccessible(true);

            try {
                field.set(target, getResource(field.getType(), target.getClass()));
            } catch (IllegalAccessException e) {
                throw new TelegRiseRuntimeException(String.format("No access to the field %s in %s requiring injection", field.getName(), target.getClass().getName()));
            }
        }
    }

    private Field[] getFieldsToInject(Class<?> clazz) {
        if (clazz == null || clazz.equals(Object.class)) return null;   // Forces the line below to return the exact copy of the first argument
        return ArrayUtils.addAll(clazz.getDeclaredFields(), getFieldsToInject(clazz.getSuperclass()));
    }

    public <T> T createInstance(Class<T> clazz){
        currentlyCreating.set(clazz);
        try {
            return clazz.cast(instanceInitializer.apply(clazz));
        } finally {
            currentlyCreating.set(null);
        }
    }

    private Object getResource(Class<?> type, Class<?> target){
        Object resource = null;
        for (Object r : resources) {
            if (type.isAssignableFrom(r.getClass())) {
                resource = r;
                break;
            }
        }

        if (resource == null){
            ResourceFactory<?> factory = resourceFactoryMap.get(type.getName());

            if (factory == null)
                throw new TelegRiseRuntimeException("Unable to find resource '" + type.getSimpleName() + "' in class '" + target.getSimpleName() + "'");
            resource = factory.getResource(target);
        }

        return resource;
    }

    public <T> T get(Class<T> tClass, Class<?> target){
        return tClass.cast(getResource(tClass, target));
    }
}