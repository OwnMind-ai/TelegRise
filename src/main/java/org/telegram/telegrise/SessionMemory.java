package org.telegram.telegrise;

import java.io.Serializable;
import java.util.Map;

public interface SessionMemory extends Serializable {
    Map<String, Serializable> getMemoryMap();

    void put(String key, Serializable value);

    Serializable get(String key);

    <T extends Serializable> T get(String key, Class<T> tClass);

    String addComponent(Serializable value);

    <T extends Serializable> T getComponent(Class<T> tClass);
}
