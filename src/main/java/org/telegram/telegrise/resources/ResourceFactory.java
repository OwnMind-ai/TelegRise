package org.telegram.telegrise.resources;

import org.jetbrains.annotations.NotNull;

public interface ResourceFactory<T> {
    @NotNull Class<T> gerResourceClass();
    T getResource(Object target);
}
