package org.telegram.telegrise.core.elements.actions;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.*;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrise.ReturnConsumer;
import org.telegram.telegrise.TelegRiseRuntimeException;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.Text;
import org.telegram.telegrise.core.elements.keyboard.Keyboard;
import org.telegram.telegrise.core.elements.media.Location;
import org.telegram.telegrise.core.elements.media.MediaType;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.InnerElement;
import org.telegram.telegrise.core.parser.TranscriptionParsingException;
import org.w3c.dom.Node;

import java.util.List;

@Element(name = "edit")
@Data @NoArgsConstructor
public class Edit implements ActionElement{
    public static final String EDIT_TEXT = "text";
    public static final String EDIT_CAPTION = "caption";
    public static final String EDIT_MEDIA = "media";
    public static final String EDIT_LIVE_LOCATION = "location";
    public static final String EDIT_MARKUP = "markup";

    @Attribute(name = "chat")
    private GeneratedValue<Long> chatId;

    @Attribute(name = "type", nullable = false)
    private GeneratedValue<String> type;

    @Attribute(name = "messageId")
    private GeneratedValue<Integer> messageId;

    @Attribute(name = "inlineMessageId")
    private GeneratedValue<String> inlineMessageId;

    @Attribute(name = "disableWebPagePreview")
    private GeneratedValue<Boolean> disableWebPagePreview;

    @Attribute(name = "inputMedia")
    private GeneratedValue<InputMedia> inputMedia;

    @InnerElement
    private Text text;
    @InnerElement
    private Keyboard keyboard;
    @InnerElement
    private MediaType media;
    @InnerElement
    private Location location;

    @Attribute(name = "returnConsumer")
    private GeneratedValue<ReturnConsumer> returnConsumer;

    @Override
    public void validate(Node node) {
        if (keyboard != null && keyboard.getCreate() == null && !keyboard.getType().equals(Keyboard.INLINE))
            throw new TranscriptionParsingException("New keyboard must be type of 'inline'", node);
    }

    @Override
    public PartialBotApiMethod<?> generateMethod(ResourcePool resourcePool) {
        switch (type.generate(resourcePool)){
            case EDIT_TEXT: return this.editText(resourcePool);
            case EDIT_CAPTION: return this.editCaption(resourcePool);
            case EDIT_MEDIA: return this.editMedia(resourcePool);
            case EDIT_MARKUP: return this.editMarkup(resourcePool);
            case EDIT_LIVE_LOCATION: return this.editLocation(resourcePool);
            default: throw new RuntimeException();
        }
    }

    private PartialBotApiMethod<?> editMarkup(ResourcePool pool) {
        if (this.keyboard == null)
            throw new TelegRiseRuntimeException("New markup is not specified");

        return EditMessageReplyMarkup.builder()
                .chatId(this.generateChatId(pool))
                .messageId(generateNullableProperty(messageId, pool))
                .inlineMessageId(generateNullableProperty(inlineMessageId, pool))
                .replyMarkup((InlineKeyboardMarkup) this.keyboard.createMarkup(pool))
                .build();
    }

    private PartialBotApiMethod<?> editLocation(ResourcePool pool) {
        if (this.location == null)
            throw new TelegRiseRuntimeException("New location is not specified");

        return EditMessageLiveLocation.builder()
                .chatId(this.generateChatId(pool))
                .messageId(generateNullableProperty(messageId, pool))
                .inlineMessageId(generateNullableProperty(inlineMessageId, pool))
                .latitude(this.location.getLatitude().generate(pool))
                .longitude(this.location.getLongitude().generate(pool))
                .horizontalAccuracy(generateNullableProperty(this.location.getHorizontalAccuracy(), pool))
                .heading(generateNullableProperty(this.location.getHeading(), pool))
                .proximityAlertRadius(generateNullableProperty(this.location.getProximityAlertRadius(), pool))
                .replyMarkup(createKeyboard(pool))
                .build();
    }

    private PartialBotApiMethod<?> editMedia(ResourcePool pool) {
        InputMedia newMedia;
        if (this.media != null)
            newMedia = this.media.createInputMedia(pool).get(0);
        else if (this.inputMedia != null)
            newMedia = inputMedia.generate(pool);
        else
            throw new TelegRiseRuntimeException("No input media passed to EditMessageMedia method");

        return EditMessageMedia.builder()
                .chatId(this.generateChatId(pool))
                .messageId(generateNullableProperty(messageId, pool))
                .inlineMessageId(generateNullableProperty(inlineMessageId, pool))
                .media(newMedia)
                .replyMarkup(createKeyboard(pool))
                .build();
    }

    private PartialBotApiMethod<?> editCaption(ResourcePool pool) {
        if (text == null)
            throw new TelegRiseRuntimeException("New caption is not specified");

        return EditMessageCaption.builder()
                .chatId(this.generateChatId(pool))
                .messageId(generateNullableProperty(messageId, pool))
                .inlineMessageId(generateNullableProperty(inlineMessageId, pool))
                .caption(text.getText().generate(pool))
                .parseMode(generateNullableProperty(text.getParseMode(), pool))
                .captionEntities(generateNullableProperty(text.getEntities(), List.of(), pool))
                .replyMarkup(createKeyboard(pool))
                .build();
    }

    private PartialBotApiMethod<?> editText(ResourcePool pool){
        if (text == null)
            throw new TelegRiseRuntimeException("New text is not specified");

        return EditMessageText.builder()
                .chatId(this.generateChatId(pool))
                .messageId(generateNullableProperty(messageId, pool))
                .inlineMessageId(generateNullableProperty(inlineMessageId, pool))
                .text(text.getText().generate(pool))
                .parseMode(generateNullableProperty(text.getParseMode(), pool))
                .entities(generateNullableProperty(text.getEntities(), List.of(), pool))
                .disableWebPagePreview( generateNullableProperty(disableWebPagePreview, pool))
                .replyMarkup(createKeyboard(pool))
                .build();
    }

    @Override
    public Long generateChatId(ResourcePool pool) {
        return this.inlineMessageId == null ? ActionElement.super.generateChatId(pool) : null;
    }

    public InlineKeyboardMarkup createKeyboard(ResourcePool pool){
        return this.keyboard != null ? (InlineKeyboardMarkup) this.keyboard.createMarkup(pool) : null;
    }
}
