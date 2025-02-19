package org.telegrise.telegrise.core.elements.media;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.actions.Send;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;

import java.util.List;

/**
 * Use this element to send audio files if you want Telegram clients to display the file as a playable voice message.
 *
 * @see <a href="https://core.telegram.org/bots/api#sendvoice">Telegram API: sendVoice</a>
 * @since 0.1
 */
@Element(name = "voice")
@Getter @Setter @NoArgsConstructor
public class Voice extends MediaType{
    /**
     * Identifier for this audio file.
     */
    @Attribute(name = "fileId")
    private GeneratedValue<String> fileId;

    /**
     * URL to the audio file.
     */
    @Attribute(name = "url")
    private GeneratedValue<String> url;

    /**
     * InputFile instance to be used as audio
     */
    @Attribute(name = "inputFile")
    private GeneratedValue<InputFile> inputFile;

    /**
     * Duration of the voice message in seconds
     */
    @Attribute(name = "duration")
    private GeneratedValue<Integer> duration;

    /**
     * Determines if this element must be executed (if returns {@code true})
     */
    @Attribute(name = "when")
    private GeneratedValue<Boolean> when = GeneratedValue.ofValue(true);

    @Override
    public PartialBotApiMethod<?> createSender(Send parent, ResourcePool pool) {
        return SendVoice.builder()
                .chatId(parent.generateChatId(pool))
                .messageThreadId( generateNullableProperty(parent.getMessageThreadId(), pool))
                .voice(this.createInputFile(pool))
                .duration(generateNullableProperty(duration, pool))
                .disableNotification( generateNullableProperty(parent.getDisableNotification(), pool))
                .protectContent( generateNullableProperty(parent.getProtectContent(), pool))
                .caption(parent.getText() != null ? parent.getText().generateText(pool) : null)
                .captionEntities(parent.getText() != null ? generateNullableProperty(parent.getText().getEntities(), List.of(), pool) : List.of())
                .parseMode(parent.getText() != null ? generateNullableProperty(parent.getText().getParseMode(), pool) : null)
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
}
