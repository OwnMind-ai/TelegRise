package org.telegram.telegrise.core.elements.media;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaBotMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrise.TelegRiseRuntimeException;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.actions.Send;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.Attribute;

import java.util.List;

@Element(name = "photo")
@Data
@NoArgsConstructor @AllArgsConstructor
public class Photo implements MediaType{
    //TODO url (treat as fileId)
    @Attribute(name = "fileId")
    private GeneratedValue<String> fileId;

    @Attribute(name = "inputFile")
    private GeneratedValue<InputFile> inputFile;

    @Attribute(name = "spoiler")
    private GeneratedValue<Boolean> spoiler;

    private InputFile createInputFile(ResourcePool pool){
        String fileId = generateNullableProperty(this.getFileId(), pool);
        InputFile file = generateNullableProperty(this.inputFile, pool);

        if (fileId != null && file != null)
            throw new TelegRiseRuntimeException("Both fileID and inputFile were passed to the SendPhoto method");
        if (fileId == null && file == null)
            throw new TelegRiseRuntimeException("inputFile or fileId cannot be null in a SendPhoto method");

        return file != null ? file : new InputFile(fileId);
    }

    @Override
    public SendMediaBotMethod<?> createSender(Send parent, ResourcePool pool) {
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
        var result = InputMediaPhoto.builder()
                .hasSpoiler(generateNullableProperty(this.spoiler, pool));

        String fileId = generateNullableProperty(this.fileId, pool);
        if (fileId == null)
            result.newMediaFile(generateNullableProperty(this.inputFile, pool).getNewMediaFile())
                    .isNewMedia(true);
        else
            result.media(fileId);

        return List.of(result.build());
    }
}
