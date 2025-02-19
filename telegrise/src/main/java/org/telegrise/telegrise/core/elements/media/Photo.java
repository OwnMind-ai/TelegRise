package org.telegrise.telegrise.core.elements.media;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.actions.Send;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;

import java.util.List;

/**
 * Represents a photo to be sent.
 *
 * @see <a href="https://core.telegram.org/bots/api#inputmediaphoto">Telegram API: InputMediaPhoto</a>
 * @since 0.1
 */
@Element(name = "photo")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Photo extends MediaType{
    /**
     * Identifier for this photo file.
     */
    @Attribute(name = "fileId")
    private GeneratedValue<String> fileId;

    /**
     * URL to the photo file.
     */
    @Attribute(name = "url")
    private GeneratedValue<String> url;

    /**
     * InputFile instance to be used as a photo
     */
    @Attribute(name = "inputFile")
    private GeneratedValue<InputFile> inputFile;

    /**
     * Set to true if the video needs to be covered with a spoiler animation
     */
    @Attribute(name = "spoiler")
    private GeneratedValue<Boolean> spoiler;

    /**
     * Determines if this element must be executed (if returns {@code true})
     */
    @Attribute(name = "when")
    private GeneratedValue<Boolean> when = GeneratedValue.ofValue(true);

    @Override
    public PartialBotApiMethod<?> createSender(Send parent, ResourcePool pool) {
       return SendPhoto.builder()
                .chatId(parent.generateChatId(pool))
                .messageThreadId( generateNullableProperty(parent.getMessageThreadId(), pool))
                .photo(this.createInputFile(pool))
                .disableNotification( generateNullableProperty(parent.getDisableNotification(), pool))
                .protectContent( generateNullableProperty(parent.getProtectContent(), pool))
                .caption(parent.getText() != null ? parent.getText().generateText(pool) : null)
                .captionEntities(parent.getText() != null ? generateNullableProperty(parent.getText().getEntities(), List.of(), pool) : List.of())
                .parseMode(parent.getText() != null ? generateNullableProperty(parent.getText().getParseMode(), pool) : null)
                .hasSpoiler(generateNullableProperty(spoiler, pool) != null)
                .replyMarkup(parent.createKeyboard(pool))
               .replyParameters(parent.createReplyParameters(pool))
                .build();
    }

    @Override
    public List<InputMedia> createInputMedia(ResourcePool pool) {
        InputMediaPhoto mediaPhoto = new InputMediaPhoto("");
        mediaPhoto.setHasSpoiler(generateNullableProperty(this.spoiler, pool));

        return List.of(this.createInputMedia(mediaPhoto, pool));
    }
}
