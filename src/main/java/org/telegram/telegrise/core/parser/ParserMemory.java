package org.telegram.telegrise.core.parser;

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

    public boolean containsKey(String name) {
        return elements.containsKey(name);
    }

    public TranscriptionElement get(String name) {
        return elements.get(name);
    }

    public TranscriptionElement put(String s, TranscriptionElement transcriptionElement) {
        return elements.put(s, transcriptionElement);
    }

    public TranscriptionElement remove(String name) {
        return elements.remove(name);
    }
}
