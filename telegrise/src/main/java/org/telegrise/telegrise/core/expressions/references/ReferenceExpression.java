package org.telegrise.telegrise.core.expressions.references;

import org.apache.commons.lang3.ClassUtils;
import org.jetbrains.annotations.NotNull;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.exceptions.TelegRiseInternalException;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegrise.telegrise.exceptions.TranscriptionParsingException;
import org.w3c.dom.Node;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public interface ReferenceExpression extends Serializable {
    Object invoke(ResourcePool pool, Object instance, Object... args) throws InvocationTargetException, IllegalAccessException;

    @NotNull Class<?>[] parameterTypes();

    @NotNull Class<?> returnType();

    default <U> GeneratedValue<U> toGeneratedValue(Class<U> type, Node node) {
        if (!isCastable(this.returnType(), type) && !ClassUtils.isAssignable(type, Void.class))
            throw new TranscriptionParsingException(String.format("Return type '%s' cannot be casted to type '%s'",
                    this.returnType().getSimpleName(), type.getSimpleName()), node);

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
                return ClassUtils.isAssignable(type, Void.class) ? null : cast(result, type);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new TelegRiseInternalException(e.getCause());
            }
        };
    }

    private static boolean isCastable(Class<?> from, Class<?> to) {
        if (ClassUtils.isAssignable(from, to)) return true;

        // Omitting type check when return is Object,
        // see org.telegrise.telegrise.core.expressions.MethodReferenceCompiler at line 115
        if (to.equals(Object.class) || from.equals(Object.class)) return true;
        return ClassUtils.isAssignable(from, Number.class) && ClassUtils.isAssignable(to, Number.class);
    }

    private static <U> U cast(Object object, Class<U> toClass) {
        try {
            return toClass.cast(object);
        } catch (ClassCastException e){
            if (!ClassUtils.isAssignable(new Class[]{object.getClass(), toClass}, Number.class)) throw e;

            var value = (Number) object;
            var number = switch (toClass.getName()) {
                case "byte", "java.lang.Byte" -> value.byteValue();
                case "short", "java.lang.Short" -> value.shortValue();
                case "int", "java.lang.Integer" -> value.intValue();
                case "long", "java.lang.Long" -> value.longValue();
                case "float", "java.lang.Float" -> value.floatValue();
                case "double", "java.lang.Double" -> value.doubleValue();
                default -> throw new IllegalArgumentException("Cannot convert " + object.getClass().getName() + " to " + toClass.getName());
            };

            return toClass.cast(number);
        }
    }
}