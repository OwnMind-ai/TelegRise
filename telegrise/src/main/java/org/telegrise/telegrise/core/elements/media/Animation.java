package org.telegrise.telegrise.core.elements.media;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaAnimation;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.actions.Send;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.types.MediaSize;

import java.util.List;

/**
 * This object represents an animation file (GIF or H.264/MPEG-4 AVC video without a sound).
 *
 * @see <a href="https://core.telegram.org/bots/api#inputmediaanimation">Telegram API: InputMediaAnimation</a>
 * @since 0.1
 */
@Element(name = "animation")
@Getter @Setter @NoArgsConstructor
public class Animation extends MediaType{
    /**
     * Identifier for this animation file.
     */
    @Attribute(name = "fileId")
    private GeneratedValue<String> fileId;

    /**
     * URL to the animation file.
     */
    @Attribute(name = "url")
    private GeneratedValue<String> url;

    /**
     * InputFile instance to be used as animation
     */
    @Attribute(name = "inputFile")
    private GeneratedValue<InputFile> inputFile;

    /**
     * Set to true if the video needs to be covered with a spoiler animation
     */
    @Attribute(name = "spoiler")
    private GeneratedValue<Boolean> spoiler;

    /**
     * Animation duration in seconds
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
     * Determines if this element must be executed (if returns {@code true})
     */
    @Attribute(name = "when")
    private GeneratedValue<Boolean> when = GeneratedValue.ofValue(true);

    @Override
    public PartialBotApiMethod<?> createSender(Send parent, ResourcePool pool) {
        MediaSize size = this.generateNullableProperty(this.size, pool);

        return SendAnimation.builder()
                .chatId(parent.generateChatId(pool))
                .messageThreadId( generateNullableProperty(parent.getMessageThreadId(), pool))
                .animation(this.createInputFile(pool))
                .duration(generateNullableProperty(duration, pool))
                .width(size != null ? size.width() : null)
                .height(size != null ? size.height() : null)
                .thumbnail(generateNullableProperty(thumbnail, pool))
                .disableNotification( generateNullableProperty(parent.getDisableNotification(), pool))
                .protectContent( generateNullableProperty(parent.getProtectContent(), pool))
                .replyParameters(parent.createReplyParameters(pool))
                .caption(parent.getText() != null ? parent.getText().generateText(pool) : null)
                .captionEntities(parent.getText() != null ? generateNullableProperty(parent.getText().getEntities(), List.of(), pool) : List.of())
                .parseMode(parent.getText() != null ? generateNullableProperty(parent.getText().getParseMode(), pool) : null)
                .hasSpoiler(generateNullableProperty(spoiler, pool) != null)
                .replyMarkup(parent.createKeyboard(pool))
                .build();
    }

    @Override
    public List<InputMedia> createInputMedia(ResourcePool pool) {
        MediaSize size = this.generateNullableProperty(this.size, pool);

        InputMediaAnimation mediaAnimation = new InputMediaAnimation("will be replaced");

        mediaAnimation.setDuration(generateNullableProperty(duration, pool));
        mediaAnimation.setWidth(size != null ? size.width() : null);
        mediaAnimation.setHeight(size != null ? size.height() : null);
        mediaAnimation.setThumbnail(generateNullableProperty(thumbnail, pool));
        mediaAnimation.setHasSpoiler(generateNullableProperty(spoiler, pool) != null);

        return List.of(this.createInputMedia(mediaAnimation, pool));
    }
}
