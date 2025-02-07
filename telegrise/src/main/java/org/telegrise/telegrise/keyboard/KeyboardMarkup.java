package org.telegrise.telegrise.keyboard;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.keyboard.Keyboard;

import java.io.Serializable;
import java.util.function.Function;

/**
 * A wrapper for {@code <keyboard>} element
 * that allows to dynamically produce {@link ReplyKeyboard} instances in current context.
 * In other words, calling {@link #createMarkup(Update)} would duplicate the behavior of {@code <keyboard>} in action.
 * Use {@link #createMarkup()} if it certain
 * that keyboard's expressions (if any) do not require {@link Update} instance to be invoked.
 *
 * @since 0.3
 */
public class KeyboardMarkup implements Serializable {
    private final Keyboard keyboard;
    private final Function<Update, ResourcePool> resourcePoolFunction;

    public KeyboardMarkup(Keyboard keyboard, Function<Update, ResourcePool> resourcePoolFunction) {
        this.keyboard = keyboard;
        this.resourcePoolFunction = resourcePoolFunction;
    }

    public ReplyKeyboard createMarkup(){
        return this.createMarkup(null);
    }

    public ReplyKeyboard createMarkup(Update update){
        return this.keyboard.createMarkup(this.resourcePoolFunction.apply(update));
    }

    public org.telegrise.telegrise.transcription.Keyboard getKeyboard(){
        return keyboard;
    }
}
