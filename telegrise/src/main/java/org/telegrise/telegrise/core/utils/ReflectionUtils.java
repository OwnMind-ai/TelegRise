package org.telegrise.telegrise.core.utils;

import lombok.Setter;
import org.apache.commons.lang3.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Function;

public class ReflectionUtils {
    @Setter
    private static Function<Object, Class<?>> classGetter = Object::getClass;

    public static <T extends Annotation> T annotation(Object o, Class<T> annotation){
        return classGetter.apply(o).getAnnotation(annotation);
    }

    public static Boolean hasAnnotation(Object o, Class<? extends Annotation> annotation){
        return classGetter.apply(o).isAnnotationPresent(annotation);
    }

    public static Class<?> getRawGenericType(Field field){
        if ( field.getGenericType() instanceof Class) return (Class<?>) field.getGenericType();

        Type type = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];

        return type instanceof Class ? (Class<?>) type : (Class<?>) ((ParameterizedType) type).getRawType();
    }

    public static boolean isAssignableAny(Class<?> aClass, Class<?>... others) {
        for (Class<?> other : others)
            if (ClassUtils.isAssignable(aClass, other)) return true;

        return false;
    }
}
