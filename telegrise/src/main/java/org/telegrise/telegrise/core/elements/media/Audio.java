package org.telegrise.telegrise.core.elements.media;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaAudio;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.actions.Send;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;

import java.util.List;

/**
 * Represents an audio file to be treated as music to be sent.
 *
 * @see <a href="https://core.telegram.org/bots/api#inputmediaaudio">Telegram API: InputMediaAudio</a>
 * @since 0.1
 */
@Element(name = "audio")
@Getter @Setter @NoArgsConstructor
public class Audio extends MediaType{
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
     * Duration of the audio in seconds
     */
    @Attribute(name = "duration")
    private GeneratedValue<Integer> duration;

    /**
     * Title of the audio
     */
    @Attribute(name = "title")
    private GeneratedValue<String> title;

    /**
     * Thumbnail of the file sent
     */
    @Attribute(name = "thumbnail")
    private GeneratedValue<InputFile> thumbnail;

    /**
     * Determines if this element must be executed (if returns {@code true})
     */
    @Attribute(name = "when")
    private GeneratedValue<Boolean> when = GeneratedValue.ofValue(true);

    @Override
    public PartialBotApiMethod<?> createSender(Send parent, ResourcePool pool) {
        return SendAudio.builder()
                .chatId(parent.generateChatId(pool))
                .messageThreadId( generateNullableProperty(parent.getMessageThreadId(), pool))
                .audio(this.createInputFile(pool))
                .duration(generateNullableProperty(duration, pool))
                .thumbnail(generateNullableProperty(thumbnail, pool))
                .title(generateNullableProperty(title, pool))
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
        InputMediaAudio mediaAudio = new InputMediaAudio("");
        mediaAudio.setDuration(generateNullableProperty(duration, pool));
        mediaAudio.setThumbnail(generateNullableProperty(thumbnail, pool));
        mediaAudio.setTitle(generateNullableProperty(title, pool));

        return List.of(this.createInputMedia(mediaAudio, pool));
    }
}
