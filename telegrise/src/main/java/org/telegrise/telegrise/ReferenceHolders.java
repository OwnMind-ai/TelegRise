package org.telegrise.telegrise;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.telegrise.telegrise.annotations.StaticReferenceHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * This class statically stores instances of all classes that have been annotated with {@link StaticReferenceHolder} annotation.
 * @since 1.0
 */
public class ReferenceHolders {
    private static final Map<String, Object> holders = new HashMap<>();

    /**
     * Returns a singleton instance of the provided class or null if not found.
     * @param tClass class object
     * @return a singleton instance of the provided class or null if not found
     * @param <T> class type
     */
    public static <T> T get(@NotNull Class<T> tClass){
        return tClass.cast(holders.get(tClass.getName()));
    }

    @ApiStatus.Internal
    public static void add(Object holder){
        holders.put(holder.getClass().getName(), holder);
    }
}
