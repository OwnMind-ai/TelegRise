package org.telegram.telegrise.core.elements.media;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.actions.Send;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;

import java.util.List;

@Element(name = "photo")
@Data
@NoArgsConstructor @AllArgsConstructor
public class Photo implements MediaType{
    @Attribute(name = "fileId")
    private GeneratedValue<String> fileId;

    @Attribute(name = "url")
    private GeneratedValue<String> url;

    @Attribute(name = "inputFile")
    private GeneratedValue<InputFile> inputFile;

    @Attribute(name = "spoiler")
    private GeneratedValue<Boolean> spoiler;



    @Override
    public PartialBotApiMethod<?> createSender(Send parent, ResourcePool pool) {
       return SendPhoto.builder()
                .chatId(parent.generateChatId(pool))
                .messageThreadId( generateNullableProperty(parent.getMessageThreadId(), pool))
                .photo(this.createInputFile(pool))
                .disableNotification( generateNullableProperty(parent.getDisableNotification(), pool))
                .protectContent( generateNullableProperty(parent.getProtectContent(), pool))
                .replyToMessageId( generateNullableProperty(parent.getReplyTo(), pool))
                .caption(parent.getText() != null ? parent.getText().getText().generate(pool) : null)
                .captionEntities(parent.getText() != null ? generateNullableProperty(parent.getText().getEntities(), List.of(), pool) : List.of())
                .parseMode(parent.getText() != null ? generateNullableProperty(parent.getText().getParseMode(), pool) : null)
                .allowSendingWithoutReply( generateNullableProperty(parent.getAllowSendingWithoutReply(), pool))
                .hasSpoiler(generateNullableProperty(spoiler, pool) != null)
                .replyMarkup(parent.createKeyboard(pool))
                .build();
    }

    @Override
    public List<InputMedia> createInputMedia(Send parent, ResourcePool pool) {
        InputMediaPhoto mediaPhoto = new InputMediaPhoto();
        mediaPhoto.setHasSpoiler(generateNullableProperty(this.spoiler, pool));

        return List.of(this.createInputMedia(mediaPhoto, pool));
    }
}
