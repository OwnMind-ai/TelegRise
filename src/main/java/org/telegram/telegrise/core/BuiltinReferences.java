package org.telegram.telegrise.core;

import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrise.SessionMemory;
import org.telegram.telegrise.annotations.HiddenParameter;
import org.telegram.telegrise.annotations.Reference;
import org.telegram.telegrise.annotations.ReferenceGenerator;
import org.telegram.telegrise.generators.GeneratedReference;
import org.telegram.telegrise.generators.GeneratedVoidReference;

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

    @ReferenceGenerator
    public GeneratedReference<String, Boolean> matches(String regex){
        return input -> input.matches(regex);
    }

    @ReferenceGenerator
    public GeneratedVoidReference<Message> register(String registry, @HiddenParameter SessionMemory memory){
        return m -> memory.putToRegistry(registry, m);
    }
}
