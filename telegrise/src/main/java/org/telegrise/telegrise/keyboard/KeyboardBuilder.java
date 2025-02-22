package org.telegrise.telegrise.keyboard;

import org.telegram.telegrambots.meta.api.interfaces.BotApiObject;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.Arrays;
import java.util.List;

/**
 * This class contains various methods for building Telegram keyboards.
 * <p>
 * Methods are designed to mimic keyboard creation in transcription files
 * using <code>row</code> and <code>button</code> tags.
 * It is recommended to import methods <code>row</code> and <code>button</code> statically.
 *
 * @since 0.10
 */
public class KeyboardBuilder {
    /**
     * Creates a basic inline keyboard button.
     * @param data inline data
     * @param text button text
     * @return inline button
     */
    public static InlineKeyboardButton button(String data, String text){
        return InlineKeyboardButton.builder().callbackData(data).text(text).build();
    }

    /**
     * Creates a basic reply keyboard button.
     * @param text button text
     * @return reply button
     */
    public static KeyboardButton button(String text){
        return KeyboardButton.builder().text(text).build();
    }

    /**
     * Creates reply keyboard row using an array of buttons' texts
     * @param buttons texts of the buttons in order
     * @return reply row
     */
    public static KeyboardRow row(String... buttons){
        return new KeyboardRow(Arrays.stream(buttons).map(KeyboardButton::new).toList());
    }

    /**
     * Creates reply or inline keyboard based on the type of the buttons passed
     * @param buttons buttons in the row
     * @return reply or inline keyboard row
     * @param <T> type of the buttons
     */
    @SafeVarargs
    public static <T extends Validable & BotApiObject> List<T> row(T... buttons){
        return List.of(buttons);
    }

    /**
     * Builds a complete inline keyboard markup based on an array of rows or lists of buttons
     * @param rows array of rows or lists of buttons
     * @return inline keyboard markup
     */
    @SafeVarargs
    public static InlineKeyboardMarkup inline(List<InlineKeyboardButton>... rows){
        return InlineKeyboardMarkup.builder().keyboard(
                Arrays.stream(rows).map(r -> r instanceof InlineKeyboardRow c ? c : new InlineKeyboardRow(r)).toList())
                .build();
    }

    /**
     * Builds a complete reply keyboard markup based on an array of rows or lists of buttons
     * @param rows array of rows or lists of buttons
     * @return reply keyboard markup
     */
    @SafeVarargs
    public static ReplyKeyboardMarkup reply(List<KeyboardButton>... rows){
        return replyBuilder(rows).build();
    }

    /**
     * Creates a reply keyboard builder based on an array of rows or lists of buttons
     * @param rows array of rows or lists of buttons
     * @return reply keyboard builder
     */
    @SafeVarargs
    public static ReplyKeyboardMarkup.ReplyKeyboardMarkupBuilder<?, ?> replyBuilder(List<KeyboardButton>... rows){
        return ReplyKeyboardMarkup.builder().keyboard(
                Arrays.stream(rows).map(r -> r instanceof KeyboardRow c ? c : new KeyboardRow(r)).toList());
    }

    public static ReplyKeyboardRemove remove(){
        return ReplyKeyboardRemove.builder().build();
    }

    public static ReplyKeyboardRemove remove(boolean selective){
        return ReplyKeyboardRemove.builder().selective(selective).build();
    }

    public static ForceReplyKeyboard force(){
        return ForceReplyKeyboard.builder().build();
    }

    public static ForceReplyKeyboard force(String placeholder){
        return ForceReplyKeyboard.builder().inputFieldPlaceholder(placeholder).build();
    }
}
