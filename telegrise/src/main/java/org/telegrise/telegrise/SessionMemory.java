package org.telegrise.telegrise;

import org.jetbrains.annotations.Nullable;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegrise.telegrise.keyboard.KeyboardState;
import org.telegrise.telegrise.transcription.Branch;
import org.telegrise.telegrise.transcription.Tree;
import org.telegrise.telegrise.types.UserRole;

import java.util.List;
import java.util.Map;

/**
 * Represents a memory of the session identified by {@link #getSessionIdentifier() session identifier}.
 * <p>
 * Implementation stores all the necessary information about the session,
 * such as session identification and role, current tree and branch, global and tree-local memory maps,
 * message registries, class-based components and basic bot information.
 * Deserialized instance of this class can be used to <b>load</b> sessions (see {@link SessionsManager}).
 * <p>
 * <b>Memory map</b> contains values associated with keys and used to store necessary information about the user across
 * {@link org.telegrise.telegrise.annotations.TreeController trees} and {@link org.telegrise.telegrise.annotations.Handler handlers}.
 * Trees have their local namespace in the memory map
 * that can be accessed using {@code `.*Local`} method variants if the current tree exists.
 * In the same way, {@code `.*Component`} methods variants use name of the provided class as a key to the
 * global memory map and can be used to store user-specific objects.
 * <p>
 * <b>Message registries</b> allow storing messages for later operations,
 * such as {@code <delete registry="registryName"/>}.
 * To put messages into a named registry, {@link #putToRegistry(String, Message)} method or {@code '::register(String)'}
 * reference generator can be used.
 *
 * @see SessionIdentifier
 * @see SessionsManager
 * @since 0.1
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface SessionMemory {
    Map<String, Object> getMemoryMap();

    /**
     * Associates the specified {@code value} with the specified {@code key} in the memory map.
     */
    void put(String key, Object value);
    /**
     * Returns true if the memory map contains a mapping for the specified {@code key}.
     */
    boolean containsKey(String key);
    /**
     * Returns the value to which the specified {@code key} is mapped,
     * or null if the memory map contains no value for the key.
     */
    Object get(String key);
    /**
     * Returns the value of type {@code T} to which the specified {@code key} is mapped,
     * or null if the memory map contains no value for the key.
     */
    <T> T get(String key, Class<T> tClass);
    /**
     * Removes the mapping for a key from the memory map if it is present.
     */
    Object remove(String key);

    /**
     * Associates the class name ({@link Class#getName()}) of the specified {@code value} to itself in the memory map.
     */
    String addComponent(Object value);
    /**
     * Returns component of type {@code T} or null if the memory map contains no component of that type.
     */
    <T> T getComponent(Class<T> tClass);
    /**
     * Removes component of type {@code T} from the memory map.
     */
    <T> T removeComponent(Class<T> tClass);
    /**
     * Returns true if the memory map contains a component of type {@code T}.
     */
    <T> boolean containsComponent(Class<T> tClass);

    private String localKey(Tree tree, String key){
        if (tree == null)
            throw new TelegRiseRuntimeException("Unable to access local memory: there is no tree to access");

        return "@" + tree.getName() + ":" + key;
    }

    /**
     * Associates the specified {@code value} with the specified {@code key} in the local namespace of specified {@code tree}.
     */
    default void put(String key, Tree tree, Object value) { put(localKey(tree, key), value); }
    /**
     * Returns true if the namespace of specified {@code tree} contains a mapping for the specified {@code key}.
     */
    default boolean containsKey(String key, Tree tree){ return containsKey(localKey(tree, key)); }
    /**
     * Returns the value to which the specified {@code key} is mapped in namespace of specified {@code Tree},
     * or null if it contains no value for the key.
     */
    default Object get(String key, Tree tree){ return get(localKey(tree, key)); }
    /**
     * Returns the value of type {@code T} to which the specified {@code key} is mapped in namespace
     * of specified {@code Tree}, or null if it contains no value for the key.
     */
    default <T> T get(String key, Tree tree, Class<T> tClass){ return get(localKey(tree, key), tClass); }
    /**
     * Removes the mapping for a key from the tree's namespace if it is present.
     */
    default Object remove(String key, Tree tree){ return remove(localKey(tree, key)); }

    /**
     * Associates the specified {@code value} with the specified {@code key} in the local namespace of the current tree.
     */
    default void putLocal(String key, Object value) { put(key, getCurrentTree(), value); }
    /**
     * Returns true if the namespace of the current tree contains a mapping for the specified {@code key}.
     */
    default boolean containsKeyLocal(String key){ return containsKey(key, getCurrentTree()); }
    /**
     * Returns the value to which the specified {@code key} is mapped in namespace of the current tree,
     * or null if it contains no value for the key.
     */
    default Object getLocal(String key){ return get(key, getCurrentTree()); }
    /**
     * Returns the value of type {@code T} to which the specified {@code key} is mapped in namespace
     * of the current tree, or null if it contains no value for the key.
     */
    default <T> T getLocal(String key, Class<T> tClass){ return get(key, getCurrentTree(), tClass); }
    /**
     * Removes the mapping for a key from the current tree's namespace if it is present.
     */
    default Object removeLocal(String key){ return remove(key, getCurrentTree()); }

    Tree getCurrentTree();
    Branch getCurrentBranch();

    UserRole getUserRole();
    void setUserRole(String roleName);
    default void setUserRole(UserRole role){
        setUserRole(role.name());   // role object can be created by user so we use a safe method instead
    }

    @Nullable Message getLastSentMessage();

    SessionIdentifier getSessionIdentifier();
    default long getUserId() { return getSessionIdentifier().getUserId(); }
    default long getChatId() { return getSessionIdentifier().getChatId(); }

    String getLanguageCode();
    void setLanguageCode(String code);

    List<Message> getRegistry(String name);
    List<Message> clearRegistry(String name);
    void putToRegistry(String name, Message message);

    KeyboardState getKeyboardState(String name, Tree parentTree);
    default KeyboardState getKeyboardState(String name){
        return getKeyboardState(name, getCurrentTree());
    }
}
