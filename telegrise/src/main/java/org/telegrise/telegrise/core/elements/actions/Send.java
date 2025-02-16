package org.telegrise.telegrise.core.elements.actions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.math.NumberUtils;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.LinkPreviewOptions;
import org.telegram.telegrambots.meta.api.objects.ReplyParameters;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.keyboard.Keyboard;
import org.telegrise.telegrise.core.elements.media.MediaType;
import org.telegrise.telegrise.core.elements.meta.LinkPreview;
import org.telegrise.telegrise.core.elements.meta.Reply;
import org.telegrise.telegrise.core.elements.text.Text;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.core.parser.InnerElement;
import org.telegrise.telegrise.core.parser.TranscriptionMemory;
import org.telegrise.telegrise.exceptions.TranscriptionParsingException;
import org.telegrise.telegrise.utils.MessageUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Use this element to send text messages.
 * <p>
 * This element corresponds to the <a href="https://core.telegram.org/bots/api#sendmessage">sendMessage</a> method.
 * {@link MessageUtils#getChat ChatId} is automatically extracted from the incoming update, but can be specified if needed.
 * It is required that this element has a text child element or at least one media element.
 * <p>
 * Text can be specified using {@link Text text} element, for use with other elements like keyboard or medias.
 * If send method sends only text, the text can be inputted directly.
 * <pre>
 * {@code
 * <send>
 *     <text>Text to send with keyboard</text>
 *     <keyboard byName="sample"/>
 * </send>
 * <send>Text to send</send>
 * }
 * <p>
 * If at least one of the media elements is specified as children to this element, then this method takes a form of the
 * corresponding media-sending API call,
 * or <a hred="https://core.telegram.org/bots/api#sendmessage">sendMediaGroup</a>
 * if more than one specified.
 * The text will become a caption to the first media item.
 * <pre>
 * {@code
 * <send>
 *     <photo url="path/to/photo"/>
 *     <text>Caption to the photo</text>
 * </send>
 * }
 *
 * @since 0.1
 * @see <a href="https://core.telegram.org/bots/api#sendmessage">Telegram API: sendMessage<a>
 */
@Element(name = "send")
@Getter @Setter @NoArgsConstructor
public class Send extends ActionElement{
    public static final String REPLY_BACK = "back";
    /**
     * Unique identifier for the target chat.
     */
    @Attribute(name = "chat")
    private GeneratedValue<Long> chatId;

    /**
     * Name of this action element for later reuse in {@link org.telegrise.telegrise.core.elements.Transition transitions}.
     */
    @Attribute(name = "name")
    private String name;

    /**
     * Determines if this element must be executed (if is {@code true})
     */
    @Attribute(name = "when")
    private GeneratedValue<Boolean> when;

    /**
     * Unique identifier for the target message thread (topic) of the forum.
     */
    @Attribute(name = "messageThreadId")
    private GeneratedValue<Integer> messageThreadId;

    /**
     * Disables webpage preview in the message. This is a shortened version of {@code <preview disabled="true"/>}.
     */
    @Attribute(name = "disablePreview")
    private GeneratedValue<Boolean> disablePreview;
    /**
     * Sends the message silently. Users will receive a notification with no sound.
     */
    @Attribute(name = "disableNotification")
    private GeneratedValue<Boolean> disableNotification;
    /**
     * Protects the contents of the sent message from forwarding and saving.
     */
    @Attribute(name = "protectContent")
    private GeneratedValue<Boolean> protectContent;
    /**
     * Set to true if a message should be sent even if the specified message to be replied to is not found.
     * This is a shortened version of {@code <reply allowSendingWithoutReply="true"/>}.
     */
    @Attribute(name = "allowSendingWithoutReply")
    private GeneratedValue<Boolean> allowSendingWithoutReply;

    /**
     * Identifier of the message that will be replied to in the current chat.
     * If value is '{@code back}',
     * the sent message will reply to the message that caused the update (by {@link MessageUtils#getMessageId}).
     * This is a shortened version of {@code <reply to="..."/>}.
     */
    @Attribute(name = "reply")
    private GeneratedValue<String> reply;

    @Attribute(name = "returnConsumer")
    private GeneratedValue<Void> returnConsumer;

    @Attribute(name = "onError")
    private GeneratedValue<Void> onError;

    @InnerElement
    private Text text;

    @InnerElement
    private List<MediaType> medias = List.of();

    @InnerElement
    private Keyboard keyboard;

    @InnerElement
    private LinkPreview linkPreview;

    @InnerElement
    private Reply replyParameters;

    @Override
    public void validate(TranscriptionMemory memory) {
        if (this.text == null && medias.isEmpty())
            throw new TranscriptionParsingException("Requires text and/or media to send", node);

        if (this.medias.size() > 1 && !this.medias.stream().allMatch(MediaType::isGroupable))
            throw new TranscriptionParsingException("Contains media types that cannot be grouped with others", node);

        if (disablePreview != null && linkPreview != null)
            throw new TranscriptionParsingException("Attribute 'disablePreview' conflicts with '<preview>' child element", node);

        if (reply != null && !reply.validate(s -> REPLY_BACK.equals(s) || NumberUtils.isDigits(s)))
            throw new TranscriptionParsingException("Attribute 'reply' must be of value 'back' or valid integer (message id)", node);

        if (replyParameters != null && reply != null)
            throw new TranscriptionParsingException("Attribute 'reply' conflicts with '<reply>' child element", node);

        if (replyParameters != null && allowSendingWithoutReply != null)
            throw new TranscriptionParsingException("Attribute 'allowSendingWithoutReply' conflicts with '<reply>' child element", node);
    }


    public ReplyKeyboard createKeyboard(ResourcePool pool){
        return this.keyboard != null ? this.keyboard.createMarkup(pool) : null;
    }

    private List<MediaType> getReadyMedias(ResourcePool pool){
        if (this.medias.isEmpty()) return this.medias;

        return this.medias.stream().filter(m -> m.getWhen().generate(pool)).collect(Collectors.toList());
    }

    @Override
    public PartialBotApiMethod<?> generateMethod(ResourcePool pool) {
        List<MediaType> readyMedias = this.getReadyMedias(pool);

        if (readyMedias.size() == 1){
            return readyMedias.getFirst().createSender(this, pool);
        } else if (readyMedias.size() > 1) {
            List<InputMedia> first = readyMedias.getFirst().createInputMedia(pool);
            assert !first.isEmpty();

            if (this.text != null){
                first.getFirst().setCaption(this.text.generateText(pool));
                first.getFirst().setParseMode(generateNullableProperty(text.getParseMode(), pool));
                first.getFirst().setCaptionEntities(generateNullableProperty(text.getEntities(), List.of(), pool));
            }

            return SendMediaGroup.builder()
                    .chatId(this.generateChatId(pool))
                    .messageThreadId( generateNullableProperty(messageThreadId, pool))
                    .medias(
                            Stream.concat(
                                    first.stream(),
                                    readyMedias.subList(1, readyMedias.size()).stream()
                                            .flatMap(m -> m.createInputMedia(pool).stream())
                            ).collect(Collectors.toList())
                    )
                    .disableNotification( generateNullableProperty(disableNotification, pool))
                    .protectContent( generateNullableProperty(protectContent, pool))
                    .replyParameters(this.createReplyParameters(pool))
                    .build();
        }

        return SendMessage.builder()
                .chatId(this.generateChatId(pool))
                .messageThreadId( generateNullableProperty(messageThreadId, pool))
                .text(text.generateText(pool))
                .parseMode(generateNullableProperty(text.getParseMode(), pool))
                .entities(generateNullableProperty(text.getEntities(), List.of(), pool))
                .disableNotification(generateNullableProperty(disableNotification, pool))
                .protectContent(generateNullableProperty(protectContent, pool))
                .replyMarkup(createKeyboard(pool))
                .linkPreviewOptions(this.createLinkPreviewOptions(pool))
                .replyParameters(this.createReplyParameters(pool))
                .build();
    }

    public ReplyParameters createReplyParameters(ResourcePool pool) {
        if (replyParameters != null) return replyParameters.produceReplyParameters(pool);
        else if (reply != null) {
             String replyValue = reply.generate(pool);
             if (replyValue == null) return null;

            return ReplyParameters.builder()
                    .messageId(replyValue.equals(REPLY_BACK) ?
                            Objects.requireNonNull(MessageUtils.getMessageId(pool.getUpdate())) : Integer.valueOf(replyValue))
                    .allowSendingWithoutReply(GeneratedValue.generate(allowSendingWithoutReply, pool))
                    .build();
        }

        return null;
    }

    public LinkPreviewOptions createLinkPreviewOptions(ResourcePool pool) {
        if (linkPreview != null) return linkPreview.producePreviewOptions(pool);
        else if (disablePreview != null) return LinkPreviewOptions.builder().isDisabled(disablePreview.generate(pool)).build();
        return null;
    }

    @Override
    public Edit toEdit() {
        Edit edit = new Edit();
        edit.setChatId(this.chatId);
        edit.setKeyboard(this.keyboard);
        edit.setText(this.text);
        edit.setWhen(this.when);
        edit.setElementNode(this.getElementNode());

        return edit;
    }
}