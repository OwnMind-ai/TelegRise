package org.telegram.telegrise.core.utils;

import java.lang.StackWalker.StackFrame;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Predicate;

public class ReflectionUtils {
    private static final int WALKER_LIMIT = 6;

    public static Class<?> getCallerClass(Predicate<Class<?>> filter){
        StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

        return walker.walk(
                s -> s.limit(WALKER_LIMIT).map(StackFrame::getDeclaringClass)
                        .filter(filter)
                        .findFirst().orElse(null)
        );
    }

    public static Class<?> getRawGenericType(Field field){
        if ( field.getGenericType() instanceof Class) return (Class<?>) field.getGenericType();

        Type type = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];

        return type instanceof Class ? (Class<?>) type : (Class<?>) ((ParameterizedType) type).getRawType();
    }
}
