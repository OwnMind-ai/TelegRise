package org.telegram.telegrise.core.elements.actions;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;

@Element(name = "answer")
@Data @NoArgsConstructor
public class Answer implements ActionElement{
    @Attribute(name = "callbackQueryId")
    private GeneratedValue<String> callbackQueryId;

    @Attribute(name = "text")
    private GeneratedValue<String> text;

    @Attribute(name = "showAlert")
    private GeneratedValue<Boolean> showAlert;

    @Attribute(name = "url")
    private GeneratedValue<String> url;

    @Attribute(name = "cacheTime")
    private GeneratedValue<Integer> cacheTime;

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
            throw new TelegRiseRuntimeException("No callbackQueryId was specified and update doesn't contains callback query");
    }

    @Override
    public GeneratedValue<Long> getChatId() {
        return null;
    }
}
