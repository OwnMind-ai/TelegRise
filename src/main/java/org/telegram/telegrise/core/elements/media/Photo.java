package org.telegram.telegrise.core.elements.media;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrise.TelegRiseRuntimeException;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.actions.Send;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.ElementField;

@Element(name = "photo")
@Data
@NoArgsConstructor
public class Photo implements MediaType{
    //TODO url (treat as fileId)
    @ElementField(name = "fileId", expression = true)
    private GeneratedValue<String> fileId;

    @ElementField(name = "inputFile", expression = true)
    private GeneratedValue<InputFile> inputFile;

    @ElementField(name = "spoiler", expression = true)
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
    public PartialBotApiMethod<?> createSender(Send parent, ResourcePool pool) {
       return SendPhoto.builder()
                .chatId(parent.generateChatId(pool))
                .photo(this.createInputFile(pool))
                .disableNotification( generateNullableProperty(parent.getDisableNotification(), pool))
                .protectContent( generateNullableProperty(parent.getProtectContent(), pool))
                .replyToMessageId( generateNullableProperty(parent.getReplyTo(), pool))
                .caption(parent.getText() != null ? parent.getText().getText().generate(pool) : null)
                .parseMode(parent.getText() != null ? parent.getText().getParseMode().generate(pool) : null)
                .build();
    }

    @Override
    public InputMedia createInputMedia(Send parent, ResourcePool pool) {
        var result = InputMediaPhoto.builder()
                .caption(parent.getText() != null ? parent.getText().getText().generate(pool) : null)
                .parseMode(parent.getText() != null ? parent.getText().getParseMode().generate(pool) : null);

        String fileId = generateNullableProperty(this.fileId, pool);
        if (fileId == null)
            result.newMediaFile(generateNullableProperty(this.inputFile, pool).getNewMediaFile())
                    .isNewMedia(true);
        else
            result.media(fileId);

        return result.build();
    }
}
