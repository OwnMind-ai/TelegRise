package org.telegram.telegrise.core;

import org.telegram.telegrise.annotations.Reference;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class BuiltinReferences {
    public static final List<String> METHODS = Arrays.stream(BuiltinReferences.class.getDeclaredMethods())
            .filter(m -> m.isAnnotationPresent(Reference.class))
            .map(Method::getName).toList();

    @Reference
    public static boolean not(boolean b){
        return !b;
    }

    @Reference
    public static boolean isNull(Object o){
        return o == null;
    }

    @Reference
    public static boolean notNull(Object o){
        return o != null;
    }

    @Reference
    public static String env(String key){
        return System.getenv(key);
    }
}
