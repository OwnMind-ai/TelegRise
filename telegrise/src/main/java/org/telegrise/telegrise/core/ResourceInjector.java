package org.telegrise.telegrise.core;

import lombok.Getter;
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
import java.util.function.BiFunction;

public final class ResourceInjector {
    private static BiFunction<Class<?>, ResourceInjector, Object> instanceInitializer = (c, i) -> {
        try {
            return c.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            String startMessage = "Cannot create instance of '" + c.getSimpleName() + "': ";

            if (e instanceof NoSuchMethodException)
                throw new TelegRiseRuntimeException(startMessage + "class must have constructor with no arguments");
            else
                throw new TelegRiseRuntimeException(startMessage + e.getMessage());
        }
    };

    @ApiStatus.Internal
    public static void setInstanceInitializer(BiFunction<Class<?>, ResourceInjector, Object> instanceInitializer) {
        ResourceInjector.instanceInitializer = instanceInitializer;
    }

    @Getter
    private final Map<String, ResourceFactory<?>> resourceFactoryMap = new HashMap<>();
    private final List<Object> resources;

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

    public void injectResources(Object target) {
        for (Field field : getFieldsToInject(target.getClass())) {
            if (!field.isAnnotationPresent(Resource.class)) continue;
            field.setAccessible(true);

            try {
                Object resource = getResource(field.getType(), target.getClass());
                if (resource == null)
                    throw new TelegRiseRuntimeException("Unable to find resource '" + field.getType().getSimpleName() + "' in class '" + target.getClass().getSimpleName() + "'");

                field.set(target, resource);
            } catch (IllegalAccessException e) {
                throw new TelegRiseRuntimeException(String.format("No access to the field %s in %s requiring injection", field.getName(), target.getClass().getName()));
            }
        }
    }

    private Field[] getFieldsToInject(Class<?> clazz) {
        if (clazz == null || clazz.equals(Object.class))
            return null;   // Forces the line below to return the exact copy of the first argument
        return ArrayUtils.addAll(clazz.getDeclaredFields(), getFieldsToInject(clazz.getSuperclass()));
    }

    public <T> T createInstance(Class<T> clazz) {
        return clazz.cast(instanceInitializer.apply(clazz, this));
    }

    private Object getResource(Class<?> type, Class<?> target) {
        Object resource = null;
        for (Object r : resources) {
            if (type.isAssignableFrom(r.getClass())) {
                resource = r;
                break;
            }
        }

        if (resource == null) {
            ResourceFactory<?> factory = resourceFactoryMap.get(type.getName());

            if (factory == null)
                return null;
            resource = factory.getResource(target);
        }

        return resource;
    }

    public <T> T get(Class<T> tClass, Class<?> target) {
        return tClass.cast(getResource(tClass, target));
    }
}