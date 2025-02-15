package org.telegrise.telegrise.types;

import org.jetbrains.annotations.ApiStatus;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.text.Text;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

/**
 * A wrapper for {@code <text>} element
 * that allows to text of the element in current context.
 * In other words, calling {@link #getText(Update)} would duplicate the behavior of {@code <text>} in action.
 * Use {@link #getText()} if it certain
 * that text's expressions (if any) do not require {@link Update} instance to be invoked.
 *
 * @since 0.3
 */
public final class TextBlock implements Serializable {
    private final Text linkedText;
    private final Function<Update, ResourcePool> resourcePoolFunction;

    @ApiStatus.Internal
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
