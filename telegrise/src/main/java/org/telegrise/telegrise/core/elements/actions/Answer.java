package org.telegrise.telegrise.core.elements.actions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.core.parser.TranscriptionMemory;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegrise.telegrise.exceptions.TranscriptionParsingException;

/**
 * Use this method to send answers to callback queries sent from inline keyboards.
 * <p>
 * This element corresponds to the <a href="https://core.telegram.org/bots/api#answercallbackquery">answerCallbackQuery</a> method.
 * ChatId and callbackQueryId are automatically extracted from the incoming update, if exists,
 * but can be specified if needed.
 * <pre>
 * {@code
 * <answer text="Done!"/>
 * }
 * </pre>
 *
 * @since 0.1
 * @see <a href="https://core.telegram.org/bots/api#answercallbackquery">Telegram API: answerCallbackQuery</a>
 */
@Element(name = "answer")
@Getter @Setter @NoArgsConstructor
public class Answer extends ActionElement{
    /**
     * Unique identifier for the query to be answered.
     */
    @Attribute(name = "callbackQueryId")
    private GeneratedValue<String> callbackQueryId;

    /**
     * Determines if this element must be executed (if returns {@code true})
     */
    @Attribute(name = "when")
    private GeneratedValue<Boolean> when;

    /**
     * Text of the notification. If not specified, nothing will be shown to the user, 0-200 characters.
     */
    @Attribute(name = "text")
    private GeneratedValue<String> text;

    /**
     * If {@code true}, an alert will be shown by the client instead of a notification at the top of the chat screen.
     */
    @Attribute(name = "showAlert")
    private GeneratedValue<Boolean> showAlert;

    /**
     * URL that will be opened by the user's client.
     */
    @Attribute(name = "url")
    private GeneratedValue<String> url;

    /**
     * The maximum amount of time in seconds that the result of the callback query may be cached client-side.
     */
    @Attribute(name = "cacheTime")
    private GeneratedValue<Integer> cacheTime;

    /**
     * Specified expression is invoked when an API error occurs; exception will not be thrown.
     * Referenced method can use parameter of type {@link TelegramApiException} to handle the exception.
     */
    @Attribute(name = "onError")
    private GeneratedValue<Void> onError;

    @Override
    public void validate(TranscriptionMemory memory) {
        if (text != null && !text.validate(s -> !s.isEmpty()))
            throw new TranscriptionParsingException("text is empty", node);
    }

    @Override
    public PartialBotApiMethod<?> generateMethod(ResourcePool resourcePool) {
        return AnswerCallbackQuery.builder()
                .callbackQueryId(this.extractCallbackQueryId(resourcePool))
                .text(generateNullableProperty(text, resourcePool))
                .showAlert(generateNullableProperty(showAlert, resourcePool))
                .url(generateNullableProperty(url, resourcePool))
                .cacheTime(generateNullableProperty(cacheTime, resourcePool))
                .build();
    }

    private String extractCallbackQueryId(ResourcePool pool){
        if (callbackQueryId != null)
            return callbackQueryId.generate(pool);
        else if (pool.getUpdate() != null && pool.getUpdate().hasCallbackQuery())
            return pool.getUpdate().getCallbackQuery().getId();
        else
            throw new TelegRiseRuntimeException("No callbackQueryId was specified and update doesn't contains callback query", node);
    }

    @Override
    public GeneratedValue<Long> getChatId() {
        return null;
    }
}
