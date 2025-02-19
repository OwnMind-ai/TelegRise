package org.telegrise.telegrise.core.elements.media;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaDocument;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.actions.Send;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;

import java.util.List;

/**
 * Represents a general file to be sent.
 *
 * @see <a href="https://core.telegram.org/bots/api#inputmediadocument">Telegram API: InputMediaDocument</a>
 * @since 0.1
 */
@Element(name = "document")
@Getter @Setter @NoArgsConstructor
public class Document extends MediaType{
    /**
     * Identifier for this document file.
     */
    @Attribute(name = "fileId")
    private GeneratedValue<String> fileId;

    /**
     * URL to the document file.
     */
    @Attribute(name = "url")
    private GeneratedValue<String> url;

    /**
     * InputFile instance to be used as a document
     */
    @Attribute(name = "inputFile")
    private GeneratedValue<InputFile> inputFile;

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
        return SendDocument.builder()
                .chatId(parent.generateChatId(pool))
                .messageThreadId( generateNullableProperty(parent.getMessageThreadId(), pool))
                .document(this.createInputFile(pool))
                .thumbnail(generateNullableProperty(thumbnail, pool))
                .disableNotification( generateNullableProperty(parent.getDisableNotification(), pool))
                .protectContent( generateNullableProperty(parent.getProtectContent(), pool))
                .caption(parent.getText() != null ? parent.getText().generateText(pool) : null)
                .captionEntities(parent.getText() != null ? generateNullableProperty(parent.getText().getEntities(), List.of(), pool) : List.of())
                .parseMode(parent.getText() != null ? generateNullableProperty(parent.getText().getParseMode(), pool) : null)
                .replyMarkup(parent.createKeyboard(pool))
                .replyParameters(parent.createReplyParameters(pool))
                .build();
    }

    @Override
    public List<InputMedia> createInputMedia(ResourcePool pool) {
        InputMediaDocument mediaDocument = new InputMediaDocument("");
        mediaDocument.setThumbnail(generateNullableProperty(thumbnail, pool));

        return List.of(this.createInputMedia(mediaDocument, pool));
    }
}
