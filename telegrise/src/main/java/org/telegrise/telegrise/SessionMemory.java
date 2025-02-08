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

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface SessionMemory {
    Map<String, Object> getMemoryMap();

    void put(String key, Object value);

    boolean containsKey(String key);

    Object get(String key);
    Object remove(String key);

    private String localKey(Tree tree, String key){
        if (tree == null)
            throw new TelegRiseRuntimeException("Unable to access local memory: there is no tree to access");

        return "@" + tree.getName() + ":" + key;
    }

    default void put(String key, Tree tree, Object value) { put(localKey(tree, key), value); }
    default boolean containsKey(String key, Tree tree){ return containsKey(localKey(tree, key)); }
    default Object get(String key, Tree tree){ return get(localKey(tree, key)); }
    default <T> T get(String key, Tree tree, Class<T> tClass){ return get(localKey(tree, key), tClass); }
    default Object remove(String key, Tree tree){ return remove(localKey(tree, key)); }

    default void putLocal(String key, Object value) { put(key, getCurrentTree(), value); }
    default boolean containsKeyLocal(String key){ return containsKey(key, getCurrentTree()); }
    default Object getLocal(String key){ return get(key, getCurrentTree()); }
    default <T> T getLocal(String key, Class<T> tClass){ return get(key, getCurrentTree(), tClass); }
    default Object removeLocal(String key){ return remove(key, getCurrentTree()); }

    Tree getCurrentTree();
    Branch getCurrentBranch();

    <T> T get(String key, Class<T> tClass);

    String addComponent(Object value);

    <T> T getComponent(Class<T> tClass);
    <T> T removeComponent(Class<T> tClass);
    <T> boolean containsComponent(Class<T> tClass);

    UserRole getUserRole();
    void setUserRole(String roleName);
    default void setUserRole(UserRole role){
        setUserRole(role.name());   // role object can be created by user so we use a safe method instead
    }

    @Nullable Message getLastSentMessage();

    String getBotUsername();
    SessionIdentifier getSessionIdentifier();

    String getLanguageCode();
    void setLanguageCode(String code);

    long getUserId();
    long getChatId();

    List<Message> getRegistry(String name);
    List<Message> clearRegistry(String name);
    void putToRegistry(String name, Message message);

    KeyboardState getKeyboardState(String name, Tree parentTree);
}
