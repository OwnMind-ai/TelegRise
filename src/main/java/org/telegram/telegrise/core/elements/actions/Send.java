package org.telegram.telegrise.core.elements.actions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrise.ReturnConsumer;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.keyboard.Keyboard;
import org.telegram.telegrise.core.elements.media.MediaType;
import org.telegram.telegrise.core.elements.text.Text;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.InnerElement;
import org.telegram.telegrise.core.parser.TranscriptionMemory;
import org.telegram.telegrise.exceptions.TranscriptionParsingException;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Element(name = "send")
@Data @NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Send extends ActionElement{
    @Attribute(name = "chat")
    private GeneratedValue<Long> chatId;

    @Attribute(name = "when")
    private GeneratedValue<Boolean> when;

    @Attribute(name = "messageThreadId")
    private GeneratedValue<Integer> messageThreadId;

    @InnerElement
    private Text text;

    @Attribute(name = "disableWebPagePreview")
    private GeneratedValue<Boolean> disableWebPagePreview;
    @Attribute(name = "disableNotification")
    private GeneratedValue<Boolean> disableNotification;
    @Attribute(name = "protectContent")
    private GeneratedValue<Boolean> protectContent;
    @Attribute(name = "allowSendingWithoutReply")
    private GeneratedValue<Boolean> allowSendingWithoutReply;

    @Attribute(name = "replyTo")
    private GeneratedValue<Integer> replyTo;

    @InnerElement
    private List<MediaType> medias = List.of();

    @InnerElement
    private Keyboard keyboard;

    @Attribute(name = "returnConsumer")
    private GeneratedValue<ReturnConsumer> returnConsumer;

    @Override
    public void validate(TranscriptionMemory memory) {
        if (this.text == null && medias.isEmpty())
            throw new TranscriptionParsingException("Requires text and/or media to send", node);

        if (this.medias.size() > 1 && !this.medias.stream().allMatch(MediaType::isGroupable))
            throw new TranscriptionParsingException("Contains media types that cannot be grouped with others", node);
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
            return readyMedias.get(0).createSender(this, pool);
        } else if (readyMedias.size() > 1) {
            List<InputMedia> first = readyMedias.get(0).createInputMedia(pool);
            assert !first.isEmpty();

            if (this.text != null){
                first.get(0).setCaption(this.text.generateText(pool));
                first.get(0).setParseMode(generateNullableProperty(text.getParseMode(), pool));
                first.get(0).setCaptionEntities(generateNullableProperty(text.getEntities(), List.of(), pool));
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
                    .replyToMessageId( generateNullableProperty(replyTo, pool))
                    .allowSendingWithoutReply( generateNullableProperty(allowSendingWithoutReply, pool))
                    .build();
        }

        return SendMessage.builder()
                .chatId(this.generateChatId(pool))
                .messageThreadId( generateNullableProperty(messageThreadId, pool))
                .text(text.generateText(pool))
                .parseMode(generateNullableProperty(text.getParseMode(), pool))
                .entities(generateNullableProperty(text.getEntities(), List.of(), pool))
                .disableWebPagePreview( generateNullableProperty(disableWebPagePreview, pool))
                .disableNotification( generateNullableProperty(disableNotification, pool))
                .protectContent( generateNullableProperty(protectContent, pool))
                .replyToMessageId( generateNullableProperty(replyTo, pool))
                .allowSendingWithoutReply( generateNullableProperty(allowSendingWithoutReply, pool))
                .replyMarkup(createKeyboard(pool))
                .build();
    }
}