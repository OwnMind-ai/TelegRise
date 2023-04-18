package org.telegram.telegrise.core.elements.media;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaAnimation;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.actions.Send;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.types.MediaSize;

import java.util.List;

@Element(name = "animation")
@Data @NoArgsConstructor
public class Animation implements MediaType{
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

    @Attribute(name = "when")
    private GeneratedValue<Boolean> when = GeneratedValue.ofValue(true);

    @Override
    public PartialBotApiMethod<?> createSender(Send parent, ResourcePool pool) {
        MediaSize size = this.generateNullableProperty(this.size, pool);

        return  SendAnimation.builder()
                .chatId(parent.generateChatId(pool))
                .messageThreadId( generateNullableProperty(parent.getMessageThreadId(), pool))
                .animation(this.createInputFile(pool))
                .duration(generateNullableProperty(duration, pool))
                .width(size != null ? size.getWidth() : null)
                .height(size != null ? size.getHeight() : null)
                .thumb(generateNullableProperty(thumbnail, pool))
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
        InputMediaAnimation mediaAnimation = new InputMediaAnimation();
        mediaAnimation.setDuration(generateNullableProperty(duration, pool));
        mediaAnimation.setWidth(size != null ? size.getWidth() : null);
        mediaAnimation.setHeight(size != null ? size.getHeight() : null);
        mediaAnimation.setThumb(generateNullableProperty(thumbnail, pool));
        mediaAnimation.setHasSpoiler(generateNullableProperty(spoiler, pool) != null);

        return List.of(this.createInputMedia(mediaAnimation, pool));
    }
}
