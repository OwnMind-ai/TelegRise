package org.telegrise.telegrise.core.elements.media;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendVideoNote;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.actions.Send;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;

import java.util.List;

/**
 * Use this element to send video messages.
 *
 * @see <a href="https://core.telegram.org/bots/api#sendvideonote">Telegram API: sendVideoNote</a>
 * @since 0.1
 */
@Element(name = "videoNote")
@Getter @Setter @NoArgsConstructor
public class VideoNote extends MediaType{
    /**
     * Identifier for this video note file.
     */
    @Attribute(name = "fileId")
    private GeneratedValue<String> fileId;

    /**
     * URL to the video note file.
     */
    @Attribute(name = "url")
    private GeneratedValue<String> url;

    /**
     * InputFile instance to be used as a video note
     */
    @Attribute(name = "inputFile")
    private GeneratedValue<InputFile> inputFile;

    /**
     * Duration of sent video in seconds
     */
    @Attribute(name = "duration")
    private GeneratedValue<Integer> duration;

    /**
     * Diameter of the video message
     */
    @Attribute(name = "length")
    private GeneratedValue<Integer> length;

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
        return SendVideoNote.builder()
                .chatId(parent.generateChatId(pool))
                .messageThreadId(generateNullableProperty(parent.getMessageThreadId(), pool))
                .videoNote(this.createInputFile(pool))
                .duration(generateNullableProperty(duration, pool))
                .length(generateNullableProperty(length, pool))
                .thumbnail(generateNullableProperty(thumbnail, pool))
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
}
