package org.telegram.telegrise.core;

import java.util.Objects;

public interface GeneratedValue<T> {
    static <V> GeneratedValue<V> ofValue(V value){
        return (resourcePool) -> value;
    }

    default boolean equalsTo(GeneratedValue<T> other, ResourcePool resourcePool){
        return other != null && Objects.equals(this.generate(resourcePool), other.generate(resourcePool));
    }

    T generate(ResourcePool resourcePool);
}
