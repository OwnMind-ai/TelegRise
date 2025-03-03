package org.telegrise.telegrise.core;

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

    private final Map<String, ResourceFactory<?>> resourceFactoryMap = new HashMap<>();
    @Setter
    private ResourceInjector parent;

    public ResourceInjector() {}

    public ResourceInjector(Object... resources) {
        addResources(resources);
    }

    public void injectResources(Object target) {
        for (Field field : getFieldsToInject(target.getClass())) {
            if (!field.isAnnotationPresent(Resource.class)) continue;
            field.setAccessible(true);

            try {
                Object resource = get(field.getType());
                if (resource == null)
                    throw new TelegRiseRuntimeException("Unable to find resource '" + field.getType().getSimpleName() + "' in class '" + target.getClass().getSimpleName() + "'");

                field.set(target, resource);
            } catch (IllegalAccessException e) {
                throw new TelegRiseRuntimeException(String.format("No access to the field %s in %s requiring injection", field.getName(), target.getClass().getName()));
            }
        }
    }

    public void addFactories(List<ResourceFactory<?>> resourceFactories) {
        resourceFactories.forEach(this::addFactory);
    }

    public void addFactory(ResourceFactory<?> factory) {
        this.resourceFactoryMap.put(factory.getResourceClass().getName(), factory);
    }

    public void addResources(Object... resources) {
        for (Object resource : resources)
            addFactory(ResourceFactory.ofInstance(resource, resource.getClass()));
    }

    private Field[] getFieldsToInject(Class<?> clazz) {
        if (clazz == null || clazz.equals(Object.class))
            return null;   // Forces the line below to return the exact copy of the first argument
        return ArrayUtils.addAll(clazz.getDeclaredFields(), getFieldsToInject(clazz.getSuperclass()));
    }

    public <T> T createInstance(Class<T> clazz) {
        return clazz.cast(instanceInitializer.apply(clazz, this));
    }

    public Object get(String name) {
        ResourceFactory<?> factory = resourceFactoryMap.get(name);
        if (factory == null && parent != null)
            return parent.get(name);

        return factory == null ? null : factory.getResource();
    }

    public <T> T get(Class<? extends T> tClass) {
        return tClass.cast(get(tClass.getName()));
    }
}