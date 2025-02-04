package org.telegrise.telegrise.keyboard;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.keyboard.Keyboard;

import java.io.Serializable;
import java.util.function.Function;

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
}
