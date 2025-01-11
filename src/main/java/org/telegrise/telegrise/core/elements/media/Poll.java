package org.telegrise.telegrise.core.elements.media;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.polls.input.InputPollOption;
import org.telegrise.telegrise.core.GeneratedValue;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.actions.Send;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;

import java.util.List;

@Element(name = "poll")
@Getter @Setter @NoArgsConstructor
public class Poll extends MediaType {
    @Attribute(name = "question", nullable = false)
    private GeneratedValue<String> question;
    @Attribute(name = "options", nullable = false)
    private GeneratedValue<List<InputPollOption>> options;
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
    @Attribute(name = "when")
    private GeneratedValue<Boolean> when = GeneratedValue.ofValue(true);

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

    @Override
    public boolean isMediaRequired() {
        return false;
    }
}
