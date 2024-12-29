package org.telegram.telegrise.core.elements.actions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.TranscriptionMemory;
import org.telegram.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegram.telegrise.exceptions.TranscriptionParsingException;

@Element(name = "answer")
@Getter @Setter @NoArgsConstructor
public class Answer extends ActionElement{
    @Attribute(name = "callbackQueryId")
    private GeneratedValue<String> callbackQueryId;

    @Attribute(name = "when")
    private GeneratedValue<Boolean> when;

    @Attribute(name = "text")
    private GeneratedValue<String> text;

    @Attribute(name = "showAlert")
    private GeneratedValue<Boolean> showAlert;

    @Attribute(name = "url")
    private GeneratedValue<String> url;

    @Attribute(name = "cacheTime")
    private GeneratedValue<Integer> cacheTime;

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
