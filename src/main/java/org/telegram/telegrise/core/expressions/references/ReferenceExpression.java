package org.telegram.telegrise.core.expressions.references;

import org.apache.commons.lang3.ClassUtils;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegram.telegrise.exceptions.TranscriptionParsingException;
import org.w3c.dom.Node;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public interface ReferenceExpression extends Serializable {
    Object invoke(ResourcePool pool, Object instance, Object... args) throws InvocationTargetException, IllegalAccessException;

    @NotNull Class<?>[] parameterTypes();
    @NotNull Class<?> returnType();

    default <U> GeneratedValue<U> toGeneratedValue(Class<U> type, Node node){
        if (!ClassUtils.isAssignable(this.returnType(), type) && !ClassUtils.isAssignable(type, Void.class))
            throw new TranscriptionParsingException(String.format("Return type '%s' cannot be casted to type '%s'",
                    this.returnType().getSimpleName(), type.getSimpleName()) , node);

        return pool -> {
            Map<Class<?>, Object> components = pool.getComponents();

            Class<?>[] types = parameterTypes();
            Object[] parameters = new Object[types.length];
            for (int i = 0; i < types.length; i++) {
                Object o = ResourcePool.extractComponent(components, types[i]);
                parameters[i] = o;

                if (o == null)
                    throw new TelegRiseRuntimeException("Unable to find '%s' in the available resources".formatted(types[i].getSimpleName()));
            }

            try {
                Object result = this.invoke(pool, pool.getHandler(), parameters);
                return ClassUtils.isAssignable(type, Void.class) ? null : type.cast(result);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e.getCause());
            }
        };
    }
}
