package org.telegram.telegrise.core.elements.media;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.actions.Send;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;

import java.util.List;

@Element(name = "poll")
@Data @NoArgsConstructor
public class Poll implements MediaType {
    @Attribute(name = "question", nullable = false)
    private GeneratedValue<String> question;
    @Attribute(name = "options", nullable = false)
    private GeneratedValue<List<String>> options;
    @Attribute(name = "isAnonymous")
    private GeneratedValue<Boolean> isAnonymous;
    @Attribute(name = "type")
    private GeneratedValue<String> type;
    @Attribute(name = "allowMultipleAnswers")
    private GeneratedValue<Boolean> allowMultipleAnswers;
    @Attribute(name = "correctOptionId")
    private GeneratedValue<Integer> correctOptionId;
    @Attribute(name = "explanation")
    private GeneratedValue<String> explanation;
    @Attribute(name = "parseMode")
    private GeneratedValue<String> parseMode;
    @Attribute(name = "entities")
    private GeneratedValue<List<MessageEntity>> entities;
    @Attribute(name = "openPeriod")
    private GeneratedValue<Integer> openPeriod;
    @Attribute(name = "closeDate")
    private GeneratedValue<Integer> closeDate;
    @Attribute(name = "isClosed")
    private GeneratedValue<Boolean> isClosed;

    @Override
    public PartialBotApiMethod<?> createSender(Send parent, ResourcePool pool) {
        return SendPoll.builder()
                .question(generateNullableProperty(question, pool))
                .options(generateNullableProperty(options, pool))
                .isAnonymous(generateNullableProperty(isAnonymous, pool))
                .type(generateNullableProperty(type, pool))
                .allowMultipleAnswers(generateNullableProperty(allowMultipleAnswers, pool))
                .correctOptionId(generateNullableProperty(correctOptionId, pool))
                .explanation(generateNullableProperty(explanation, pool))
                .explanationParseMode(generateNullableProperty(parseMode, pool))
                .explanationEntities(generateNullableProperty(entities, pool))
                .openPeriod(generateNullableProperty(openPeriod, pool))
                .closeDate(generateNullableProperty(closeDate, pool))
                .isClosed(generateNullableProperty(isClosed, pool))
                .disableNotification( generateNullableProperty(parent.getDisableNotification(), pool))
                .protectContent( generateNullableProperty(parent.getProtectContent(), pool))
                .replyToMessageId( generateNullableProperty(parent.getReplyTo(), pool))
                .allowSendingWithoutReply( generateNullableProperty(parent.getAllowSendingWithoutReply(), pool))
                .replyMarkup(parent.createKeyboard(pool))
                .build();
    }

    @Override
    public List<InputMedia> createInputMedia(ResourcePool pool) {
        return null;
    }

    @Override
    public boolean isGroupable() {
        return false;
    }

    @Override
    public GeneratedValue<String> getFileId() {
        return null;
    }

    @Override
    public GeneratedValue<String> getUrl() {
        return null;
    }

    @Override
    public GeneratedValue<InputFile> getInputFile() {
        return null;
    }
}
