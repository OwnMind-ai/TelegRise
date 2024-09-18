package org.telegram.telegrise;

import org.jetbrains.annotations.Nullable;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrise.types.UserRole;

import java.io.Serializable;
import java.util.Map;
import java.util.List;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface SessionMemory extends Serializable {
    Map<String, Serializable> getMemoryMap();

    void put(String key, Serializable value);

    boolean containsKey(String key);

    Serializable get(String key);
    Serializable remove(String key);

    <T extends Serializable> T get(String key, Class<T> tClass);

    String addComponent(Serializable value);

    <T extends Serializable> T getComponent(Class<T> tClass);
    <T extends Serializable> T removeComponent(Class<T> tClass);
    <T extends Serializable> boolean containsComponent(Class<T> tClass);

    UserRole getUserRole();

    @Nullable Message getLastSentMessage();

    String getBotUsername();
    UserIdentifier getUserIdentifier();

    String getLanguageCode();
    void setLanguageCode(String code);

    long getUserId();
    long getChatId();

    List<Message> getRegister(String name);
    List<Message> clearRegister(String name);
    void putToRegister(String name, Message message);
}
