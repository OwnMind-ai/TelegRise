package org.telegram.telegrise.core.expressions.references;

import org.apache.commons.lang3.ClassUtils;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.exceptions.TranscriptionParsingException;
import org.w3c.dom.Node;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public interface ReferenceExpression extends Serializable {
    Object invoke(Object instance, Object... args) throws InvocationTargetException, IllegalAccessException;

    @NotNull Class<?>[] parameterTypes();
    @NotNull Class<?> returnType();

    default <U> GeneratedValue<U> toGeneratedValue(Class<U> type, Node node){
        if (!ClassUtils.isAssignable(this.returnType(), type) && !ClassUtils.isAssignable(type, Void.class))
            throw new TranscriptionParsingException(String.format("Return type '%s' cannot be casted to type '%s'",
                    this.returnType().getSimpleName(), type.getSimpleName()) , node);

        return pool -> {
            Map<Class<?>, Object> components = pool.getComponents();

            if (!Arrays.stream(parameterTypes()).allMatch(p -> ResourcePool.extractComponent(components, p) != null))
                throw new TelegRiseRuntimeException("Illegal parameters set: {" + Arrays.stream(parameterTypes())
                        .map(Class::getSimpleName).collect(Collectors.joining(", ")) + "}");

            Object[] parameters = Arrays.stream(parameterTypes())
                    .map(p -> ResourcePool.extractComponent(components, p)).toArray();

            try {
                Object result = this.invoke(pool.getHandler(), parameters);
                return ClassUtils.isAssignable(type, Void.class) ? null : (U) result;
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e.getCause());
            }
        };
    }
}
