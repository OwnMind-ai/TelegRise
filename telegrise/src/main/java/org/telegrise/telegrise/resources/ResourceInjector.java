package org.telegrise.telegrise.resources;

import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;
import org.telegrise.telegrise.annotations.Resource;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiStatus.Internal
public final class ResourceInjector {
    private final List<Object> resources;
    @Getter
    private final Map<String, ResourceFactory<?>> resourceFactoryMap = new HashMap<>();

    public ResourceInjector(Object... resources) {
        this.resources = List.of(resources);
    }

    public ResourceInjector(List<ResourceFactory<?>> resourceFactories, Object... resources) {
        this.resources = List.of(resources);
        this.addFactories(resourceFactories);
    }

    public void addFactories(List<ResourceFactory<?>> resourceFactories) {
        resourceFactories.forEach(f -> this.resourceFactoryMap.put(f.gerResourceClass().getName(), f));
    }

    public void injectResources(Object target){
        for (Field field : getFieldsToInject(target.getClass())) {
            if (!field.isAnnotationPresent(Resource.class)) continue;
            field.setAccessible(true);

            Class<?> type = field.getType();
            Object resource = resources.stream().filter(r -> r.equals(type))
                    .findFirst().orElseGet(() -> resources.stream().filter(r -> type.isAssignableFrom(r.getClass()))
                            .findFirst().orElse(null));

            if (resource == null){
                ResourceFactory<?> factory = resourceFactoryMap.get(type.getName());

                if (factory == null)
                    throw new TelegRiseRuntimeException("Unable to find resource '" + type.getSimpleName() + "' in class '" + target.getClass().getSimpleName() + "'");
                resource = factory.getResource(target);
            }

            try {
                field.set(target, resource);
            } catch (IllegalAccessException e) {
                throw new TelegRiseRuntimeException(String.format("No access to the field %s in %s requiring injection", field.getName(), target.getClass().getName()));
            }
        }
    }

    private Field[] getFieldsToInject(Class<?> clazz) {
        if (clazz == null || clazz.equals(Object.class)) return null;   // Forces the line below to return the exact copy of the first argument
        return ArrayUtils.addAll(clazz.getDeclaredFields(), getFieldsToInject(clazz.getSuperclass()));
    }
}
