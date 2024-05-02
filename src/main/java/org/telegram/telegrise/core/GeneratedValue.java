package org.telegram.telegrise.core;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Predicate;

@FunctionalInterface
public interface GeneratedValue<T> extends Serializable {
    String ABSTRACT_METHOD_NAME = "generate";

    static <V> GeneratedValue<V> ofValue(V value){
        return new StaticValue<>(value);
    }

    default boolean equalsTo(GeneratedValue<?> other, ResourcePool resourcePool){
        return other != null && Objects.equals(this.generate(resourcePool), other.generate(resourcePool));
    }

    default boolean validate(Predicate<T> predicate){
        return true;
    }

    T generate(ResourcePool resourcePool);

    class StaticValue<T> implements GeneratedValue<T>{
        private final T value;

        public StaticValue(T value) {
            this.value = value;
        }

        @Override
        public T generate(ResourcePool resourcePool) {
            return value;
        }

        @Override
        public boolean validate(Predicate<T> predicate) {
            return predicate.test(value);
        }
    }
}
