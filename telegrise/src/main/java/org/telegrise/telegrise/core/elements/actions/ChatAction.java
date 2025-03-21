package org.telegrise.telegrise.core.elements.actions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.core.parser.TranscriptionMemory;
import org.telegrise.telegrise.exceptions.TranscriptionParsingException;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Use this method when you need to tell the user that something is happening on the bot's side.
 * <p>
 * This element corresponds to the <a href="https://core.telegram.org/bots/api#sendchataction">sendChatAction</a> method.
 * {@link org.telegrise.telegrise.utils.MessageUtils#getChat ChatId} is automatically extracted from the incoming update, but can be specified if needed.
 * It is required that this element has a text child element or at least one media element.
 * <pre>
 * {@code
 * <chatAction action="typing"/>
 * }
 * </pre>
 *
 * @since 0.1
 * @see <a href="https://core.telegram.org/bots/api#sendchataction">Telegram API: sendChatAction</a>
 */
@Element(name = "chatAction")
@Getter @Setter @NoArgsConstructor
public class ChatAction extends ActionElement{
    private static final Set<String> ACTIONS = Set.of("typing", "upload_photo", "record_video", "upload_video",
            "record_voice", "upload_voice", "upload_document", "choose_sticker", "find_location", "record_video_note", "upload_video_note");

    /**
     * Unique identifier for the target chat.
     */
    @Attribute(name = "chat")
    private GeneratedValue<Long> chatId;

    /**
     * Determines if this element must be executed (if returns {@code true})
     */
    @Attribute(name = "when")
    private GeneratedValue<Boolean> when;

    /**
     * Unique identifier for the target message thread.
     */
    @Attribute(name = "messageThreadId")
    private GeneratedValue<Integer> messageThreadId;

    /**
     * Type of action to broadcast
     */
    @Attribute(name = "action", nullable = false)
    private GeneratedValue<String> action;

    /**
     * Specified expression is invoked when an API error occurs; exception will not be thrown.
     * Referenced method can use parameter of type {@link TelegramApiException} to handle the exception.
     */
    @Attribute(name = "onError")
    private GeneratedValue<Void> onError;

    @Override
    public void validate(TranscriptionMemory memory) {
        if (!action.validate(ACTIONS::contains))
            throw new TranscriptionParsingException("Unrecognized chat action. Chat action could be one of following: " +
                    ACTIONS.stream().sorted().collect(Collectors.joining(", ")), node);
    }

    @Override
    public PartialBotApiMethod<?> generateMethod(ResourcePool resourcePool) {
        return SendChatAction.builder()
                .chatId(generateChatId(resourcePool))
                .messageThreadId(generateNullableProperty(messageThreadId, resourcePool))
                .action(action.generate(resourcePool))
                .build();
    }
}
