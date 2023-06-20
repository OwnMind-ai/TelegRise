package org.telegram.telegrise.resources;

import org.jetbrains.annotations.NotNull;

public interface ResourceFactory<T> {
    static <U> ResourceFactory<U> ofInstance(U instance, Class<U> uClass){
        return new ResourceFactory<>() {
            @Override
            public @NotNull Class<U> gerResourceClass() {
                return uClass;
            }

            @Override
            public U getResource(Object target) {
                return instance;
            }
        };
    }

    @NotNull Class<T> gerResourceClass();
    T getResource(Object target);
}
