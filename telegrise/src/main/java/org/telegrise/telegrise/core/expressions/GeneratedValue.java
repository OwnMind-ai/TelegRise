package org.telegrise.telegrise.core.expressions;

import org.jetbrains.annotations.Nullable;
import org.telegrise.telegrise.Expression;
import org.telegrise.telegrise.core.ResourcePool;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

@FunctionalInterface
public interface GeneratedValue<T> extends Serializable {
    GeneratedValue<Boolean> GENERATED_FALSE = GeneratedValue.ofValue(false);
    GeneratedValue<Boolean> GENERATED_TRUE = GeneratedValue.ofValue(true);

    String ABSTRACT_METHOD_NAME = "generate";

    static <V> GeneratedValue<V> ofValue(V value){
        return new StaticValue<>(value);
    }

    static <V> @Nullable V generate(@Nullable GeneratedValue<V> generatedValue, ResourcePool pool){
        return generatedValue == null ? null : generatedValue.generate(pool);
    }

    static <V> Optional<V> generateOptional(@Nullable GeneratedValue<V> generatedValue, ResourcePool pool){
        return generatedValue == null ? Optional.empty() : Optional.of(generatedValue.generate(pool));
    }

    default boolean equalsTo(GeneratedValue<?> other, ResourcePool resourcePool){
        return other != null && Objects.equals(this.generate(resourcePool), other.generate(resourcePool));
    }

    default boolean validate(Predicate<T> predicate){
        return true;
    }

    T generate(ResourcePool resourcePool);

    default Expression<T> toExpression(){
        return new Expression<>(this);
    }

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
