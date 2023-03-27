package org.telegram.telegrise.core.elements.media;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaDocument;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.actions.Send;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;

import java.util.List;

@Element(name = "document")
@Data @NoArgsConstructor
public class Document implements MediaType{
    @Attribute(name = "fileId")
    private GeneratedValue<String> fileId;

    @Attribute(name = "url")
    private GeneratedValue<String> url;

    @Attribute(name = "inputFile")
    private GeneratedValue<InputFile> inputFile;

    @Attribute(name = "thumbnail")
    private GeneratedValue<InputFile> thumbnail;

    @Override
    public PartialBotApiMethod<?> createSender(Send parent, ResourcePool pool) {
        return SendDocument.builder()
                .chatId(parent.generateChatId(pool))
                .messageThreadId( generateNullableProperty(parent.getMessageThreadId(), pool))
                .document(this.createInputFile(pool))
                .thumb(generateNullableProperty(thumbnail, pool))
                .disableNotification( generateNullableProperty(parent.getDisableNotification(), pool))
                .protectContent( generateNullableProperty(parent.getProtectContent(), pool))
                .replyToMessageId( generateNullableProperty(parent.getReplyTo(), pool))
                .caption(parent.getText() != null ? parent.getText().getText().generate(pool) : null)
                .captionEntities(parent.getText() != null ? generateNullableProperty(parent.getText().getEntities(), List.of(), pool) : List.of())
                .parseMode(parent.getText() != null ? generateNullableProperty(parent.getText().getParseMode(), pool) : null)
                .allowSendingWithoutReply( generateNullableProperty(parent.getAllowSendingWithoutReply(), pool))
                .replyMarkup(parent.createKeyboard(pool))
                .build();
    }

    @Override
    public List<InputMedia> createInputMedia(ResourcePool pool) {
        InputMediaDocument mediaDocument = new InputMediaDocument();
        mediaDocument.setThumb(generateNullableProperty(thumbnail, pool));

        return List.of(this.createInputMedia(mediaDocument, pool));
    }
}
