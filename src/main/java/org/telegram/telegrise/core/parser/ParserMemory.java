package org.telegram.telegrise.core.parser;

import org.telegram.telegrise.TelegRiseRuntimeException;
import org.telegram.telegrise.core.elements.TranscriptionElement;

import java.util.HashMap;
import java.util.Map;

public final class ParserMemory {
    private final Map<String, TranscriptionElement> elements = new HashMap<>();

    public int size() {
        return elements.size();
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public TranscriptionElement get(String name) {
        return elements.get(name);
    }

    public void put(String name, TranscriptionElement transcriptionElement) {
        if (elements.containsKey(name))
            throw new TelegRiseRuntimeException("Name '" + name + "' already exists");

        elements.put(name, transcriptionElement);
    }

    public void set(String name, TranscriptionElement transcriptionElement){
        elements.put(name, transcriptionElement);
    }
}
