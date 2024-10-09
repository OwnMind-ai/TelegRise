package org.telegram.telegrise;

import org.jetbrains.annotations.Nullable;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrise.types.UserRole;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface SessionMemory {
    Map<String, Object> getMemoryMap();

    void put(String key, Object value);

    boolean containsKey(String key);

    Object get(String key);
    Object remove(String key);

    <T> T get(String key, Class<T> tClass);

    String addComponent(Object value);

    <T> T getComponent(Class<T> tClass);
    <T> T removeComponent(Class<T> tClass);
    <T> boolean containsComponent(Class<T> tClass);

    UserRole getUserRole();

    @Nullable Message getLastSentMessage();

    String getBotUsername();
    UserIdentifier getUserIdentifier();

    String getLanguageCode();
    void setLanguageCode(String code);

    long getUserId();
    long getChatId();

    List<Message> getRegistry(String name);
    List<Message> clearRegistry(String name);
    void putToRegistry(String name, Message message);
}
