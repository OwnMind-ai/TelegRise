package org.telegram.telegrise.core;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrise.core.elements.invocation.Variables;

import java.util.Objects;

public interface GeneratedValue<T> {
    static <V> GeneratedValue<V> ofValue(V value){
        return (update, variables) -> value;
    }

    default boolean equalsTo(GeneratedValue<T> other, Update update, Variables variables){
        return other != null && Objects.equals(this.generate(update, variables), other.generate(update, variables));
    }

    T generate(Update update, Variables variables);
}
