package org.telegrise.telegrise.builtin;

import org.jetbrains.annotations.ApiStatus;
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

/**
 * This class contains builtin references and generators that can be used without static-referencing.
 * <pre>
 * {@code
 * <invoke when='#env("VAR") -> #notNull AND #memory("var") -> #not' method='false -> :store("var")' />
 * }
 *
 * @since 0.8
 */
public class BuiltinReferences {
    @ApiStatus.Internal
    public static final List<String> METHODS = Arrays.stream(BuiltinReferences.class.getDeclaredMethods())
            .filter(m -> m.isAnnotationPresent(Reference.class))
            .map(Method::getName).toList();

    @ApiStatus.Internal
    public static final List<String> GENERATORS = Arrays.stream(BuiltinReferences.class.getDeclaredMethods())
            .filter(m -> m.isAnnotationPresent(ReferenceGenerator.class))
            .map(Method::getName).toList();

    /**
     * Logical NOT.
     * @param b boolean value
     * @return {@code !b}
     */
    @Reference
    public static boolean not(boolean b){
        return !b;
    }

    /**
     * @param o an object to test
     * @return true if the object is null
     */
    @Reference
    public static boolean isNull(Object o){
        return o == null;
    }

    /**
     * @param o an object to test
     * @return true if the object is not null
     */
    @Reference
    public static boolean notNull(Object o){
        return o != null;
    }

    /**
     * Retrieves an environmental variable by name.
     * <pre>
     * {@code
     * <bot token='#env("BOT_TOKEN")'>
     * }
     *
     * @param key name of the variable
     * @return string value of that variable or null if there is none
     */
    @Reference
    public static String env(String key){
        return System.getenv(key);
    }

    /**
     * Produces {@link GeneratedReference} that returns {@code true}
     * if passed in reference matches {@code regex} parameter.
     * <pre>
     * {@code
     * <branch when='#messageText -> ::matches("\\d+")'>
     * }
     *
     * @param regex a pattern to be matched with
     * @return generated reference
     */
    @ReferenceGenerator
    public static GeneratedReference<String, Boolean> matches(String regex){
        return input -> input.matches(regex);
    }

    /**
     * Produces {@link GeneratedVoidReference} that stores
     * passed in {@link Message} object to message registry named {@code registry}.
     * <pre>
     * {@code
     * <send returnConsumer='::register("toDelete")'>
     * }
     *
     * @return generated reference
     */
    @ReferenceGenerator
    public static GeneratedVoidReference<Message> register(String registry, @HiddenParameter SessionMemory memory) {
        return m -> memory.putToRegistry(registry, m);
    }

    /**
     * Produces {@link GeneratedReference} that stores
     * passed in an object into <b>tree-local</b> memory under the key
     * {@code name} and returns the passed in object.
     *
     * @param name map key
     * @return generated reference
     */
    @ReferenceGenerator
    public static GeneratedReference<Object, Object> store(String name, @HiddenParameter SessionMemory memory){
        return o -> {
            memory.put(name, memory.getCurrentTree(),  o);
            return o;
        };
    }

    /**
     * Produces {@link GeneratedReference} that stores
     * passed in an object into <b>global</b> memory under the key
     * {@code name} and returns the passed in object.
     *
     * @param name map key
     * @return generated reference
     */
    @ReferenceGenerator
    public static GeneratedReference<Object, Object> storeGlobal(String name, @HiddenParameter SessionMemory memory){
        return o -> {
            memory.put(name, o);
            return o;
        };
    }

    /**
     * Retrieves an object stored in <b>tree-local</b> memory with a given key {@code name}.
     *
     * @param name map key
     * @return object in memory or null
     */
    @Reference
    public static Object memory(String name, @HiddenParameter SessionMemory memory){
        return memory.get(name, memory.getCurrentTree());
    }

    /**
     * Retrieves an object stored in <b>global</b> memory with a given key {@code name}.
     *
     * @param name map key
     * @return object in memory or null
     */
    @Reference
    public static Object memoryGlobal(String name, @HiddenParameter SessionMemory memory){
        return memory.get(name);
    }

    /**
     * Marks branch or section of expression as "TO DO" and it <b>should not</b> appear in production code.
     * <p>
     * If this reference gets to be executed in runtime, it will throw {@code UnsupportedOperationException}.
     * Parser will log a warning if this reference ever occurs.
     */
    @Reference
    public static Object todo(){
        throw new UnsupportedOperationException("TODO");
    }
}
