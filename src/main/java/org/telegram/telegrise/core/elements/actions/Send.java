package org.telegram.telegrise.core.elements.actions;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrise.ReturnConsumer;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.Text;
import org.telegram.telegrise.core.elements.keyboard.Keyboard;
import org.telegram.telegrise.core.elements.media.MediaType;
import org.telegram.telegrise.core.parser.*;
import org.w3c.dom.Node;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Element(name = "send")
@Data @NoArgsConstructor
public class Send implements ActionElement{
    @Attribute(name = "chat")
    private GeneratedValue<Long> chatId;

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
    public void validate(Node node, TranscriptionMemory memory) {
        if (!this.medias.stream().allMatch(MediaType::isGroupable))
            throw new TranscriptionParsingException("Contains media types that cannot be grouped with others", node);
    }


    public ReplyKeyboard createKeyboard(ResourcePool pool){
        return this.keyboard != null ? this.keyboard.createMarkup(pool) : null;
    }

    @Override
    public PartialBotApiMethod<?> generateMethod(ResourcePool pool) {
        if (medias.size() == 1){
            return this.medias.get(0).createSender(this, pool);
        } else if (medias.size() > 1) {
            List<InputMedia> first = this.medias.get(0).createInputMedia(pool);
            assert first.size() > 0;

            if (this.text != null){
                first.get(0).setCaption(this.text.getText().generate(pool));
                first.get(0).setParseMode(generateNullableProperty(text.getParseMode(), pool));
                first.get(0).setCaptionEntities(generateNullableProperty(text.getEntities(), List.of(), pool));
            }

            return SendMediaGroup.builder()
                    .chatId(this.generateChatId(pool))
                    .messageThreadId( generateNullableProperty(messageThreadId, pool))
                    .medias(
                            Stream.concat(
                                    first.stream(),
                                    this.medias.subList(1, this.medias.size()).stream()
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
                .text(text.getText().generate(pool))
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