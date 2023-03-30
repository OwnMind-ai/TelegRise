package org.telegram.telegrise.core.elements.actions;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;

@Element(name = "answer")
@Data @NoArgsConstructor
public class Answer implements ActionElement{
    @Attribute(name = "callbackQueryId", nullable = false)
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
                .callbackQueryId(callbackQueryId.generate(resourcePool))
                .text(generateNullableProperty(text, resourcePool))
                .showAlert(generateNullableProperty(showAlert, resourcePool))
                .url(generateNullableProperty(url, resourcePool))
                .cacheTime(generateNullableProperty(cacheTime, resourcePool))
                .build();
    }

    @Override
    public GeneratedValue<Long> getChatId() {
        return null;
    }
}
