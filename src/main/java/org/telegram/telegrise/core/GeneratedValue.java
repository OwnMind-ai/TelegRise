package org.telegram.telegrise.core;

import java.io.Serializable;
import java.util.Objects;

public interface GeneratedValue<T> extends Serializable {
    String ABSTRACT_METHOD_NAME = "generate";

    static <V> GeneratedValue<V> ofValue(V value){
        return (resourcePool) -> value;
    }

    default boolean equalsTo(GeneratedValue<?> other, ResourcePool resourcePool){
        return other != null && Objects.equals(this.generate(resourcePool), other.generate(resourcePool));
    }

    T generate(ResourcePool resourcePool);
}
