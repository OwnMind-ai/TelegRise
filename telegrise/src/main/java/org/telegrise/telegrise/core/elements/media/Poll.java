package org.telegrise.telegrise.core.elements.media;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.polls.input.InputPollOption;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.actions.Send;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;

import java.util.List;

/**
 * Use this element to send a native poll.
 *
 * @see <a href="https://core.telegram.org/bots/api#sendpoll">Telegram API: sendPoll</a>
 * @since 0.1
 */
@Element(name = "poll")
@Getter @Setter @NoArgsConstructor
public class Poll extends MediaType {
    /* TODO IMPROVEMENTS:
        |> Add option to define list of answer as child elements
        |> Question and explanation can have formation, so they MUST have their separate element:
        |  <poll>
        |      <question>Sample <b>question</b>?</question>
        |      <explanation>Sample <b>explanation</b>.</explanation>
        |  </poll>
        |  In this example, elements <question> and <explanation> are just <text> element with another name
        |  (and must be internally like that, use inheritance)
    */

    /**
     * Poll question, 1-300 characters
     */
    @Attribute(name = "question", nullable = false)
    private GeneratedValue<String> question;
    /**
     * A list of 2-10 answer options
     */
    @Attribute(name = "options", nullable = false)
    private GeneratedValue<List<InputPollOption>> options;
    /**
     * Set to true if the poll needs to be anonymous
     */
    @Attribute(name = "isAnonymous")
    private GeneratedValue<Boolean> isAnonymous;
    /**
     * Poll type, "quiz" or "regular"
     */
    @Attribute(name = "type")
    private GeneratedValue<String> type;
    /**
     * Set to true, if the poll allows multiple answers, ignored for polls in quiz mode
     */
    @Attribute(name = "allowMultipleAnswers")
    private GeneratedValue<Boolean> allowMultipleAnswers;
    /**
     * 0-based identifier of the correct answer option, required for polls in quiz mode
     */
    @Attribute(name = "correctOptionId")
    private GeneratedValue<Integer> correctOptionId;
    /**
     * Text that is shown when a user chooses an incorrect answer or taps on the lamp icon in a quiz-style poll
     */
    @Attribute(name = "explanation")
    private GeneratedValue<String> explanation;
    /**
     * Amount of time in seconds the poll will be active after creation, 5-600. Can't be used together with close_date.
     */
    @Attribute(name = "openPeriod")
    private GeneratedValue<Integer> openPeriod;
    /**
     * Point in time (Unix timestamp) when the poll will be automatically closed.
     */
    @Attribute(name = "closeDate")
    private GeneratedValue<Integer> closeDate;
    /**
     * Set to true if the poll needs to be immediately closed. This can be useful for poll preview.
     */
    @Attribute(name = "isClosed")
    private GeneratedValue<Boolean> isClosed;
    /**
     * Determines if this element must be executed (if returns {@code true})
     */
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
                .openPeriod(generateNullableProperty(openPeriod, pool))
                .closeDate(generateNullableProperty(closeDate, pool))
                .isClosed(generateNullableProperty(isClosed, pool))
                .disableNotification( generateNullableProperty(parent.getDisableNotification(), pool))
                .protectContent( generateNullableProperty(parent.getProtectContent(), pool))
                .replyMarkup(parent.createKeyboard(pool))
                .replyParameters(parent.createReplyParameters(pool))
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
