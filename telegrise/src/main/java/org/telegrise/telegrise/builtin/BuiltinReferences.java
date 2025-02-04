package org.telegrise.telegrise.builtin;

import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegrise.telegrise.SessionMemory;
import org.telegrise.telegrise.annotations.HiddenParameter;
import org.telegrise.telegrise.annotations.Reference;
import org.telegrise.telegrise.annotations.ReferenceGenerator;
import org.telegrise.telegrise.generators.GeneratedReference;
import org.telegrise.telegrise.generators.GeneratedVoidReference;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class BuiltinReferences {
    public static final List<String> METHODS = Arrays.stream(BuiltinReferences.class.getDeclaredMethods())
            .filter(m -> m.isAnnotationPresent(Reference.class))
            .map(Method::getName).toList();

    public static final List<String> GENERATORS = Arrays.stream(BuiltinReferences.class.getDeclaredMethods())
            .filter(m -> m.isAnnotationPresent(ReferenceGenerator.class))
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
    public static GeneratedReference<String, Boolean> matches(String regex){
        return input -> input.matches(regex);
    }

    @ReferenceGenerator
    public static GeneratedVoidReference<Message> register(String registry, @HiddenParameter SessionMemory memory){
        return m -> memory.putToRegistry(registry, m);
    }

    @ReferenceGenerator
    public static GeneratedReference<Object, Object> store(String name, @HiddenParameter SessionMemory memory){
        return o -> {
            memory.put(name, memory.getCurrentTree(),  o);
            return o;
        };
    }

    @ReferenceGenerator
    public static GeneratedReference<Object, Object> storeGlobal(String name, @HiddenParameter SessionMemory memory){
        return o -> {
            memory.put(name, o);
            return o;
        };
    }

    @Reference
    public static Object memory(String name, @HiddenParameter SessionMemory memory){
        return memory.get(name, memory.getCurrentTree());
    }

    @Reference
    public static Object memoryGlobal(String name, @HiddenParameter SessionMemory memory){
        return memory.get(name);
    }

    @Reference
    public static Object todo(){
        throw new UnsupportedOperationException("TODO");
    }
}
