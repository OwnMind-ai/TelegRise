package org.telegram.telegrise.types;

import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.text.Text;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

public final class TextBlock implements Serializable {
    private final Text linkedText;
    private final Function<Update, ResourcePool> resourcePoolFunction;

    public TextBlock(Text linkedText, Function<Update, ResourcePool> resourcePoolFunction) {
        this.linkedText = linkedText;
        this.resourcePoolFunction = resourcePoolFunction;
    }

    public String getText(){
        return this.getText(null);
    }

    public String getText(Update update){
        return linkedText.generateText(this.resourcePoolFunction.apply(update));
    }

    public List<MessageEntity> getEntities(Update update){
        return linkedText.getEntities().generate(this.resourcePoolFunction.apply(update));
    }

    public String getParseMode(Update update){
        return linkedText.getParseMode().generate(this.resourcePoolFunction.apply(update));
    }
}
