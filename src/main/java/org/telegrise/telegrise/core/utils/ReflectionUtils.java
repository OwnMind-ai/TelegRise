package org.telegrise.telegrise.core.utils;

import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ReflectionUtils {
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
