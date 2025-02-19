package org.telegrise.telegrise.core.elements.media;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaVideo;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.actions.Send;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.types.MediaSize;

import java.util.List;

/**
 * Represents a video to be sent.
 *
 * @see <a href="https://core.telegram.org/bots/api#inputmediavideo">Telegram API: InputMediaVideo</a>
 * @since 0.1
 */
@Element(name = "video")
@Getter @Setter @NoArgsConstructor
public class Video extends MediaType {
    /**
     * Identifier for this video file.
     */
    @Attribute(name = "fileId")
    private GeneratedValue<String> fileId;

    /**
     * URL to the url file.
     */
    @Attribute(name = "url")
    private GeneratedValue<String> url;

    /**
     * InputFile instance to be used as a video
     */
    @Attribute(name = "inputFile")
    private GeneratedValue<InputFile> inputFile;

    /**
     * Set to true if the video needs to be covered with a spoiler animation
     */
    @Attribute(name = "spoiler")
    private GeneratedValue<Boolean> spoiler;

    /**
     * Video duration in seconds
     */
    @Attribute(name = "duration")
    private GeneratedValue<Integer> duration;

    /**
     * Width and height of the animation
     */
    @Attribute(name = "size")
    private GeneratedValue<MediaSize> size;

    /**
     * Thumbnail of the file sent
     */
    @Attribute(name = "thumbnail")
    private GeneratedValue<InputFile> thumbnail;

    /**
     * Set to true if the uploaded video is suitable for streaming
     */
    @Attribute(name = "supportsStreaming")
    private GeneratedValue<Boolean> supportsStreaming;

    /**
     * Determines if this element must be executed (if returns {@code true})
     */
    @Attribute(name = "when")
    private GeneratedValue<Boolean> when = GeneratedValue.ofValue(true);

    @Override
    public PartialBotApiMethod<?> createSender(Send parent, ResourcePool pool) {
        MediaSize size = this.generateNullableProperty(this.size, pool);

        return SendVideo.builder()
                .chatId(parent.generateChatId(pool))
                .messageThreadId( generateNullableProperty(parent.getMessageThreadId(), pool))
                .video(this.createInputFile(pool))
                .duration(generateNullableProperty(duration, pool))
                .width(size != null ? size.width() : null)
                .height(size != null ? size.height() : null)
                .thumbnail(generateNullableProperty(thumbnail, pool))
                .supportsStreaming(generateNullableProperty(this.supportsStreaming, pool))
                .disableNotification( generateNullableProperty(parent.getDisableNotification(), pool))
                .protectContent( generateNullableProperty(parent.getProtectContent(), pool))
                .caption(parent.getText() != null ? parent.getText().generateText(pool) : null)
                .captionEntities(parent.getText() != null ? generateNullableProperty(parent.getText().getEntities(), List.of(), pool) : List.of())
                .parseMode(parent.getText() != null ? generateNullableProperty(parent.getText().getParseMode(), pool) : null)
                .hasSpoiler(generateNullableProperty(spoiler, pool) != null)
                .replyParameters(parent.createReplyParameters(pool))
                .replyMarkup(parent.createKeyboard(pool))
                .build();
    }

    @Override
    public List<InputMedia> createInputMedia(ResourcePool pool) {
        MediaSize size = this.generateNullableProperty(this.size, pool);
        InputMediaVideo mediaVideo = new InputMediaVideo("");
        mediaVideo.setDuration(generateNullableProperty(duration, pool));
        mediaVideo.setWidth(size != null ? size.width() : null);
        mediaVideo.setHeight(size != null ? size.height() : null);
        mediaVideo.setThumbnail(generateNullableProperty(thumbnail, pool));
        mediaVideo.setSupportsStreaming(generateNullableProperty(this.supportsStreaming, pool));
        mediaVideo.setHasSpoiler(generateNullableProperty(spoiler, pool) != null);

        return List.of(this.createInputMedia(mediaVideo, pool));
    }
}
