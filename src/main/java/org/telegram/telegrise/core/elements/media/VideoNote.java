package org.telegram.telegrise.core.elements.media;

import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendVideoNote;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.actions.Send;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Element(name = "videoNote")
@Getter @Setter @NoArgsConstructor
public class VideoNote extends MediaType{
    @Attribute(name = "fileId")
    private GeneratedValue<String> fileId;

    @Attribute(name = "url")
    private GeneratedValue<String> url;

    @Attribute(name = "inputFile")
    private GeneratedValue<InputFile> inputFile;

    @Attribute(name = "duration")
    private GeneratedValue<Integer> duration;

    @Attribute(name = "length")
    private GeneratedValue<Integer> length;

    @Attribute(name = "thumbnail")
    private GeneratedValue<InputFile> thumbnail;

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
}
