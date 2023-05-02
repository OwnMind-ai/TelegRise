package org.telegram.telegrise;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.BotTranscription;
import org.telegram.telegrise.core.elements.InteractiveElement;
import org.telegram.telegrise.core.parser.TranscriptionMemory;
import org.telegram.telegrise.types.KeyboardMarkup;
import org.telegram.telegrise.types.TextBlock;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InteractiveObjectManager {
    private final Map<String, Serializable> objects = new HashMap<>();
    private final Function<Update, ResourcePool> resourcePoolProducer;

    public InteractiveObjectManager(Function<Update, ResourcePool> resourcePoolProducer) {
        this.resourcePoolProducer = resourcePoolProducer;
    }

    public void load(BotTranscription transcription){
        TranscriptionMemory memory = transcription.getMemory();
        objects.putAll(memory.getElements().entrySet().stream().parallel()
                .filter(e -> e.getValue() instanceof InteractiveElement)
                .map(e -> Map.entry(e.getKey(), ((InteractiveElement<?>) e.getValue()).createInteractiveObject(this.resourcePoolProducer)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );
    }

    private void checkName(String name){
        if (!this.objects.containsKey(name))
            throw new TelegRiseRuntimeException("Element named '" + name + "' does not exist");
    }

    @SuppressWarnings("SameParameterValue")
    private void checkType(String name, Class<?> type){
        if (!type.isInstance(this.objects.get(name)))
            throw new TelegRiseRuntimeException("Element named '" + name + "' does not inherit type '" + type.getSimpleName() + "'");
    }

    public <T extends Serializable> T get(String name, Class<T> tClass){
        checkName(name);
        checkType(name, tClass);

        return tClass.cast(this.objects.get(name));
    }

    public TextBlock getTextBlock(String name){
        checkName(name);
        checkType(name, TextBlock.class);

        return (TextBlock) this.objects.get(name);
    }

    private KeyboardMarkup getKeyboardMarkup(String name){
        checkName(name);
        checkType(name, KeyboardMarkup.class);

        return (KeyboardMarkup) this.objects.get(name);
    }

    //TODO getTextBlock(name, lang)
}
