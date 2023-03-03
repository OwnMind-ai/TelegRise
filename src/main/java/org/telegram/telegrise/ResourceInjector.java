package org.telegram.telegrise;

import org.telegram.telegrise.annotations.Resource;

import java.lang.reflect.Field;
import java.util.List;

public class ResourceInjector {
    private final List<Object> resources;

    public ResourceInjector(Object... resources) {
        this.resources = List.of(resources);
    }

    public void injectResources(Object target){
        for (Field field : target.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Resource.class)) {
                field.setAccessible(true);

                Class<?> type = field.getType();
                Object resource = resources.stream().filter(r -> type.isAssignableFrom(r.getClass()))
                        .findFirst().orElseThrow(() -> new TelegRiseRuntimeException(
                                "Unable to find resource " + type.getSimpleName() + " in class '" + target.getClass().getSimpleName() + "'"));

                try {
                    field.set(target, resource);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
