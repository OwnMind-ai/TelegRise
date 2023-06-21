package org.telegram.telegrise.core.utils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ReflectionUtils {

    public static Class<?> getRawGenericType(Field field){
        if ( field.getGenericType() instanceof Class) return (Class<?>) field.getGenericType();

        Type type = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];

        return type instanceof Class ? (Class<?>) type : (Class<?>) ((ParameterizedType) type).getRawType();
    }
}
