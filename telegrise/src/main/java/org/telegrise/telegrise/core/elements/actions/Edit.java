package org.telegrise.telegrise.core.elements.actions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.*;
import org.telegram.telegrambots.meta.api.objects.LinkPreviewOptions;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.keyboard.Keyboard;
import org.telegrise.telegrise.core.elements.media.Location;
import org.telegrise.telegrise.core.elements.media.MediaType;
import org.telegrise.telegrise.core.elements.meta.LinkPreview;
import org.telegrise.telegrise.core.elements.text.Text;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.core.parser.InnerElement;
import org.telegrise.telegrise.core.parser.TranscriptionMemory;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegrise.telegrise.exceptions.TranscriptionParsingException;
import org.telegrise.telegrise.types.ApiResponse;
import org.telegrise.telegrise.utils.MessageUtils;

import java.util.List;
import java.util.Set;

/**
 * Use this element to edit any message.
 * <p>
 * This element corresponds to one of the edit methods in <a href="https://core.telegram.org/bots/api#updating-messages">"Updating messages"</a>
 * sections, depending on its parameters:
 * if {@code type} attribute is not stated, the element will choose one automatically
 * depending on the type of message to edit and whether a text, media, location, and/or keyboard were provided.
 * {@link MessageUtils#getChat ChatId} and {@link MessageUtils#getMessageId messageId} are automatically extracted from the incoming update,
 * but can be specified if needed.
 * <p>
 * Text can be specified using {@link Text text} element, for use with other elements like keyboard or medias.
 * If this method edits only text, the text can be inputted directly.
 * <pre>
 * {@code
 * <edit>
 *     <text>Text to edit with keyboard</text>
 *     <keyboard byName="sample"/>
 * </edit>
 * <edit>Text to edit</edit>
 * }
 * </pre>
 *
 * @since 0.1
 * @see <a href="https://core.telegram.org/bots/api#updating-messages">Telegram API: Updating messages</a>
 */
@Element(name = "edit", finishAfterParsing = true)
@Getter @Setter @NoArgsConstructor
public class Edit extends ActionElement{
    public static final String EDIT_TEXT = "text";
    public static final String EDIT_CAPTION = "caption";
    public static final String EDIT_MEDIA = "media";
    public static final String EDIT_LIVE_LOCATION = "location";
    public static final String EDIT_MARKUP = "markup";
    public static final Set<String> TYPES = Set.of(EDIT_LIVE_LOCATION, EDIT_TEXT, EDIT_CAPTION, EDIT_MEDIA, EDIT_MARKUP);

    public static final String CALLBACK = "callback";
    public static final String LAST = "last";
    //TODO add auto

    /**
     * Unique identifier for the target chat.
     */
    @Attribute(name = "chat")
    private GeneratedValue<Long> chatId;

    /**
     * Determines if this element must be executed (if returns {@code true})
     */
    @Attribute(name = "when")
    private GeneratedValue<Boolean> when;

    /**
     * Type of the edit method, such as {@code 'text'}, {@code 'caption'},
     * {@code 'media'}, {@code 'location'} or {@code 'markup'}.
     * If this attribute is not defined, the element will choose one automatically
     * depending on the type of message to edit and its parameters.
     */
    @Attribute(name = "type")
    private GeneratedValue<String> type;

    /**
     * Name of this action element for later use in {@link org.telegrise.telegrise.core.elements.Transition transitions}.
     */
    @Attribute(name = "name")
    private String name;

    /**
     * Source of the message to be edited.
     */
    @Attribute(name = "source")
    private String source = LAST;

    /**
     * Identifier of the message to edit.
     */
    @Attribute(name = "messageId")
    private GeneratedValue<Integer> messageId;

    /**
     * Identifier of the inline message.
     */
    @Attribute(name = "inlineMessageId")
    private GeneratedValue<String> inlineMessageId;

    /**
     * Disables webpage preview in the message. This is a shortened version of {@code <preview disabled="true"/>}.
     */
    @Attribute(name = "disablePreview")
    private GeneratedValue<Boolean> disablePreview;

    /**
     * A new media content of the message.
     */
    @Attribute(name = "inputMedia")
    private GeneratedValue<InputMedia> inputMedia;

    /**
     * Provides a result of the executed API call (a boolean).
     * Method references can use parameter of type {@code boolean} or {@link ApiResponse} to access returned value.
     */
    @Attribute(name = "returnConsumer")
    private GeneratedValue<Void> returnConsumer;

    /**
     * Specified expression is invoked when an API error occurs; exception will not be thrown.
     * Referenced method can use parameter of type {@link TelegramApiException} to handle the exception.
     */
    @Attribute(name = "onError")
    private GeneratedValue<Void> onError;

    @InnerElement
    private Text text;
    @InnerElement
    private Keyboard keyboard;
    @InnerElement
    private MediaType media;
    @InnerElement(priority = 1)
    private Location location;

    @InnerElement
    private LinkPreview linkPreview;

    @Override
    public void validate(TranscriptionMemory memory) {
        if (type != null && !type.validate(TYPES::contains))
            throw new TranscriptionParsingException("Unrecognized type. Type could be one of the following: " +
                    String.join(", ", TYPES), node);

        if (keyboard != null && keyboard.getCreate() == null && !Keyboard.INLINE.equals(keyboard.getType()))
            throw new TranscriptionParsingException("New keyboard must be type of 'inline'", node);

        if (!LAST.equals(source) && !CALLBACK.equals(source))
            throw new TranscriptionParsingException("Invalid message source '" + source + "', possible sources are: '"
                    + LAST + "' or '" + CALLBACK + "'" , node);
    }

    private Message extractMessage(ResourcePool pool){
        if (LAST.equals(this.getSource())){
            if (pool.getMemory().getLastSentMessage() == null)
                throw new TelegRiseRuntimeException("Unable to apply refresh element: last sent message doesn't exists", node);

            return pool.getMemory().getLastSentMessage();
        } else if (CALLBACK.equals(this.getSource())) {
            if (pool.getUpdate() == null || !pool.getUpdate().hasCallbackQuery())
                throw new TelegRiseRuntimeException("Unable to apply refresh element: passed update has no callback query", node);

            MaybeInaccessibleMessage maybeInaccessibleMessage = pool.getUpdate().getCallbackQuery().getMessage();

            if (!(maybeInaccessibleMessage instanceof Message))
                throw new TelegRiseRuntimeException("Unable to apply refresh element: passed callback query refers to inaccessible message", node);

            return (Message) maybeInaccessibleMessage;
        }

        throw new TelegRiseRuntimeException("Unable to apply refresh element: unknown refresh type " + this.getType(), node);
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
        } else if (this.keyboard != null){
            return EDIT_MARKUP;
        }

        throw new TelegRiseRuntimeException("Unknown edit type", node);
    }

    private InlineKeyboardMarkup getMarkup(ResourcePool pool){
        if (this.keyboard != null)
            return (InlineKeyboardMarkup) this.keyboard.createMarkup(pool);

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
            throw new TelegRiseRuntimeException("New location is not specified", node);

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
            newMedia = this.media.createInputMedia(pool).getFirst();
        else if (this.inputMedia != null)
            newMedia = inputMedia.generate(pool);

        if (newMedia == null)
            throw new TelegRiseRuntimeException("No input media passed to EditMessageMedia method", node);

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
            throw new TelegRiseRuntimeException("New caption is not specified", node);

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
            throw new TelegRiseRuntimeException("New text is not specified", node);

        return EditMessageText.builder()
                .chatId(this.generateChatId(pool))
                .messageId(messageId)
                .inlineMessageId(generateNullableProperty(inlineMessageId, pool))
                .text(text.generateText(pool))
                .parseMode(generateNullableProperty(text.getParseMode(), pool))
                .entities(generateNullableProperty(text.getEntities(), List.of(), pool))
                .linkPreviewOptions(createLinkPreviewOptions(pool))
                .replyMarkup(this.getMarkup(pool))
                .build();
    }

    public LinkPreviewOptions createLinkPreviewOptions(ResourcePool pool) {
        if (linkPreview != null) return linkPreview.producePreviewOptions(pool);
        else if (disablePreview != null) return LinkPreviewOptions.builder().isDisabled(disablePreview.generate(pool)).build();
        return null;
    }

    @Override
    public Long generateChatId(ResourcePool pool) {
        return this.inlineMessageId == null ? super.generateChatId(pool) : null;
    }

    @Override
    public Edit toEdit() {
        return this;
    }
}
