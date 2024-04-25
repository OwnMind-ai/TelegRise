package org.telegram.telegrise.core.parser;

import lombok.Getter;
import org.telegram.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegram.telegrise.core.elements.BotTranscription;
import org.telegram.telegrise.core.elements.TranscriptionElement;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class TranscriptionMemory implements Serializable {
    @Getter
    private final Map<String, TranscriptionElement> elements = new HashMap<>();
    @Getter
    private final List<Consumer<BotTranscription>> tasks = new LinkedList<>();
    private boolean readOnly = false;

    public int size() {
        return elements.size();
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public TranscriptionElement get(String name) {
        return elements.get(name);
    }

    public <T> T get(String name, Class<T> tClass, List<String> possibleTags){
        TranscriptionElement result = elements.get(name);

        if (result == null) throw new TelegRiseRuntimeException("Element named '" + name + "' does not exist");

        if (!tClass.isAssignableFrom(result.getClass()))
            throw new TelegRiseRuntimeException(String.format("Element '%s' represents the <%s> tag, required: %s",
                    name, result.getClass().getAnnotation(Element.class).name(),
                    possibleTags.stream().map(s -> "<" + s + ">").collect(Collectors.joining(" or "))
            ));

        return tClass.cast(result);
    }

    public void put(String name, TranscriptionElement transcriptionElement) {
        if (readOnly) throw new UnsupportedOperationException();

        if (elements.containsKey(name))
            throw new TelegRiseRuntimeException("Name '" + name + "' already exists");

        elements.put(name, transcriptionElement);
    }

    public void set(String name, TranscriptionElement transcriptionElement){
        if (readOnly) throw new UnsupportedOperationException();
        elements.put(name, transcriptionElement);
    }

    public boolean containsKey(String key){
        return this.elements.containsKey(key);
    }

    public void setReadOnly(){
        this.readOnly = true;
    }
}
