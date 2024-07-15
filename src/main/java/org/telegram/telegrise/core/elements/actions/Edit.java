package org.telegram.telegrise.core.elements.actions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.*;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrise.ReturnConsumer;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.NodeElement;
import org.telegram.telegrise.core.elements.keyboard.Keyboard;
import org.telegram.telegrise.core.elements.media.Location;
import org.telegram.telegrise.core.elements.media.MediaType;
import org.telegram.telegrise.core.elements.text.Text;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.InnerElement;
import org.telegram.telegrise.core.parser.TranscriptionMemory;
import org.telegram.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegram.telegrise.exceptions.TranscriptionParsingException;
import org.telegram.telegrise.keyboard.DynamicKeyboard;
import org.telegram.telegrise.utils.MessageUtils;
import org.w3c.dom.Node;

import java.util.List;
import java.util.Set;

@Element(name = "edit")
@Data @NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Edit extends NodeElement implements ActionElement{
    public static final String EDIT_TEXT = "text";
    public static final String EDIT_CAPTION = "caption";
    public static final String EDIT_MEDIA = "media";
    public static final String EDIT_LIVE_LOCATION = "location";
    public static final String EDIT_MARKUP = "markup";
    public static final Set<String> TYPES = Set.of(EDIT_LIVE_LOCATION, EDIT_TEXT, EDIT_CAPTION, EDIT_MEDIA, EDIT_MARKUP);

    public static final String CALLBACK = "callback";
    public static final String LAST = "last";

    @Attribute(name = "chat")
    private GeneratedValue<Long> chatId;

    @Attribute(name = "type")
    private GeneratedValue<String> type;

    @Attribute(name = "keyboardId")
    private String keyboardId;

    @Attribute(name = "source")
    private String source = LAST;

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
    @InnerElement(priority = 1)
    private Location location;

    @Attribute(name = "returnConsumer")
    private GeneratedValue<ReturnConsumer> returnConsumer;

    @Override
    public void validate(Node node, TranscriptionMemory memory) {
        if (type != null && !type.validate(TYPES::contains))
            throw new TranscriptionParsingException("Unrecognized type. Type could be one of the following: " +
                    String.join(", ", TYPES), node);

        if (keyboard != null && keyboard.getCreate() == null && !keyboard.getType().equals(Keyboard.INLINE))
            throw new TranscriptionParsingException("New keyboard must be type of 'inline'", node);

        if (!LAST.equals(source) && !CALLBACK.equals(source))
            throw new TranscriptionParsingException("Invalid message source '" + source + "', possible sources are: '"
                    + LAST + "' or '" + CALLBACK + "'" , node);

        if (keyboard != null && keyboardId != null)
            throw new TranscriptionParsingException("KeyboardId and keyboard element conflict with each other", node);
    }

    @Override
    public void load(TranscriptionMemory memory) {
        if (this.keyboardId != null && this.text == null && this.media == null && this.location == null)
            this.type = GeneratedValue.ofValue(EDIT_MARKUP);
    }

    private Message extractMessage(ResourcePool pool){
        if (LAST.equals(this.getSource())){
            if (pool.getMemory().getLastSentMessage() == null)
                throw new TelegRiseRuntimeException("Unable to apply refresh element: last sent message doesn't exists");

            return pool.getMemory().getLastSentMessage();
        } else if (CALLBACK.equals(this.getSource())) {
            if (pool.getUpdate() == null || !pool.getUpdate().hasCallbackQuery())
                throw new TelegRiseRuntimeException("Unable to apply refresh element: passed update has no callback query");

            MaybeInaccessibleMessage maybeInaccessibleMessage = pool.getUpdate().getCallbackQuery().getMessage();

            if (!(maybeInaccessibleMessage instanceof Message))
                throw new TelegRiseRuntimeException("Unable to apply refresh element: passed callback query refers to inaccessible message");

            return (Message) maybeInaccessibleMessage;
        }

        throw new TelegRiseRuntimeException("Unable to apply refresh element: unknown refresh type " + this.getType());
    }

    @Override
    public PartialBotApiMethod<?> generateMethod(ResourcePool resourcePool) {
        Message message = this.extractMessage(resourcePool);
        Integer messageId = message != null ? message.getMessageId() : null;

        if (this.messageId != null)
            messageId = this.messageId.generate(resourcePool);

        String type = this.type != null ? this.type.generate(resourcePool) : this.getTypeFromMessage(message);

        return switch (type) {
            case EDIT_TEXT -> this.editText(resourcePool, messageId);
            case EDIT_CAPTION -> this.editCaption(resourcePool, messageId);
            case EDIT_MEDIA -> this.editMedia(resourcePool, messageId);
            case EDIT_MARKUP -> this.editMarkup(resourcePool, messageId);
            case EDIT_LIVE_LOCATION -> this.editLocation(resourcePool, messageId);
            default -> throw new RuntimeException();
        };
    }

    private String getTypeFromMessage(Message message) {
        if(this.location != null) {
            return EDIT_LIVE_LOCATION;
        } else if (this.media != null) {
            return EDIT_MEDIA;
        } else if (this.getText() != null){
            return MessageUtils.hasMedia(message) ? EDIT_CAPTION : EDIT_TEXT;
        } else if (this.keyboard != null || this.keyboardId != null){
            return EDIT_MARKUP;
        }

        throw new TelegRiseRuntimeException("Unknown edit type");
    }

    private InlineKeyboardMarkup getMarkup(ResourcePool pool){
        if (this.keyboard != null)
            return (InlineKeyboardMarkup) this.keyboard.createMarkup(pool);

        if (this.keyboardId != null)
            return pool.getMemory().get(this.keyboardId, DynamicKeyboard.class).createInline(pool);

        return null;
    }

    private PartialBotApiMethod<?> editMarkup(ResourcePool pool, Integer messageId) {
        return EditMessageReplyMarkup.builder()
                .chatId(this.generateChatId(pool))
                .messageId(messageId)
                .inlineMessageId(generateNullableProperty(inlineMessageId, pool))
                .replyMarkup(this.getMarkup(pool))
                .build();
    }

    private PartialBotApiMethod<?> editLocation(ResourcePool pool, Integer messageId) {
        if (this.location == null)
            throw new TelegRiseRuntimeException("New location is not specified");

        return EditMessageLiveLocation.builder()
                .chatId(this.generateChatId(pool))
                .messageId(messageId)
                .inlineMessageId(generateNullableProperty(inlineMessageId, pool))
                .latitude(this.location.getLatitude().generate(pool))
                .longitude(this.location.getLongitude().generate(pool))
                .horizontalAccuracy(generateNullableProperty(this.location.getHorizontalAccuracy(), pool))
                .heading(generateNullableProperty(this.location.getHeading(), pool))
                .proximityAlertRadius(generateNullableProperty(this.location.getProximityAlertRadius(), pool))
                .replyMarkup(this.getMarkup(pool))
                .build();
    }

    private PartialBotApiMethod<?> editMedia(ResourcePool pool, Integer messageId) {
        InputMedia newMedia = null;
        if (this.media != null)
            newMedia = this.media.createInputMedia(pool).get(0);
        else if (this.inputMedia != null)
            newMedia = inputMedia.generate(pool);

        if (newMedia == null)
            throw new TelegRiseRuntimeException("No input media passed to EditMessageMedia method");

        if (this.text != null){
            newMedia.setCaption(this.text.generateText(pool));
            newMedia.setParseMode(this.text.getParseMode() == null ? null : this.text.getParseMode().generate(pool));
            newMedia.setCaptionEntities(this.text.getEntities() == null ? List.of() : this.text.getEntities().generate(pool));
        }

        return EditMessageMedia.builder()
                .chatId(this.generateChatId(pool))
                .messageId(messageId)
                .inlineMessageId(generateNullableProperty(inlineMessageId, pool))
                .media(newMedia)
                .replyMarkup(this.getMarkup(pool))
                .build();
    }

    private PartialBotApiMethod<?> editCaption(ResourcePool pool, Integer messageId) {
        if (text == null)
            throw new TelegRiseRuntimeException("New caption is not specified");

        return EditMessageCaption.builder()
                .chatId(this.generateChatId(pool))
                .messageId(messageId)
                .inlineMessageId(generateNullableProperty(inlineMessageId, pool))
                .caption(text.generateText(pool))
                .parseMode(generateNullableProperty(text.getParseMode(), pool))
                .captionEntities(generateNullableProperty(text.getEntities(), List.of(), pool))
                .replyMarkup(this.getMarkup(pool))
                .build();
    }

    private PartialBotApiMethod<?> editText(ResourcePool pool, Integer messageId){
        if (text == null)
            throw new TelegRiseRuntimeException("New text is not specified");

        return EditMessageText.builder()
                .chatId(this.generateChatId(pool))
                .messageId(messageId)
                .inlineMessageId(generateNullableProperty(inlineMessageId, pool))
                .text(text.generateText(pool))
                .parseMode(generateNullableProperty(text.getParseMode(), pool))
                .entities(generateNullableProperty(text.getEntities(), List.of(), pool))
                .disableWebPagePreview( generateNullableProperty(disableWebPagePreview, pool))
                .replyMarkup(this.getMarkup(pool))
                .build();
    }

    @Override
    public Long generateChatId(ResourcePool pool) {
        return this.inlineMessageId == null ? ActionElement.super.generateChatId(pool) : null;
    }
}
