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

@Element(name = "video")
@Getter @Setter @NoArgsConstructor
public class Video extends MediaType {
    @Attribute(name = "fileId")
    private GeneratedValue<String> fileId;

    @Attribute(name = "url")
    private GeneratedValue<String> url;

    @Attribute(name = "inputFile")
    private GeneratedValue<InputFile> inputFile;

    @Attribute(name = "spoiler")
    private GeneratedValue<Boolean> spoiler;

    @Attribute(name = "duration")
    private GeneratedValue<Integer> duration;

    @Attribute(name = "size")
    private GeneratedValue<MediaSize> size;

    @Attribute(name = "thumbnail")
    private GeneratedValue<InputFile> thumbnail;

    @Attribute(name = "supportsStreaming")
    private GeneratedValue<Boolean> supportsStreaming;

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
                .replyToMessageId( generateNullableProperty(parent.getReplyTo(), pool))
                .caption(parent.getText() != null ? parent.getText().generateText(pool) : null)
                .captionEntities(parent.getText() != null ? generateNullableProperty(parent.getText().getEntities(), List.of(), pool) : List.of())
                .parseMode(parent.getText() != null ? generateNullableProperty(parent.getText().getParseMode(), pool) : null)
                .allowSendingWithoutReply( generateNullableProperty(parent.getAllowSendingWithoutReply(), pool))
                .hasSpoiler(generateNullableProperty(spoiler, pool) != null)
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
