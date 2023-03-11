package org.telegram.telegrise.core.elements.actions;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrise.MessageUtils;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.Text;
import org.telegram.telegrise.core.elements.media.MediaType;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.ElementField;
import org.telegram.telegrise.core.parser.InnerElement;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Element(name = "send")
@Data @NoArgsConstructor
public class Send implements ActionElement{
    @ElementField(name = "chat", expression = true)
    private GeneratedValue<Long> chatId;
    @InnerElement
    private Text text;

    @ElementField(name = "disableWebPagePreview", expression = true)
    private GeneratedValue<Boolean> disableWebPagePreview;
    @ElementField(name = "disableNotification", expression = true)
    private GeneratedValue<Boolean> disableNotification;
    @ElementField(name = "protectContent", expression = true)
    private GeneratedValue<Boolean> protectContent;
    @ElementField(name = "allowSendingWithoutReply", expression = true)
    private GeneratedValue<Boolean> allowSendingWithoutReply;
    @ElementField(name = "replyTo", expression = true)
    private GeneratedValue<Integer> replyTo;

    @InnerElement
    private List<MediaType> medias = List.of();

    //TODO inner element of <keyboard> ; add other fields
    private GeneratedValue<ReplyKeyboard> replyMarkup;

    public long generateChatId(ResourcePool pool){
        return chatId != null ? chatId.generate(pool) : Objects.requireNonNull(MessageUtils.getChat(pool.getUpdate())).getId();
    }

    @Override
    public PartialBotApiMethod<?> generateMethod(ResourcePool pool) {
        if (medias.size() == 1){
            return this.medias.get(0).createSender(this, pool);
        } else if (medias.size() > 1) {
            List<InputMedia> first = this.medias.get(0).createInputMedia(this, pool);
            assert first.size() > 0;

            if (this.text != null){
                first.get(0).setCaption(this.text.getText().generate(pool));
                first.get(0).setParseMode(this.text.getParseMode().generate(pool));
            }

            return SendMediaGroup.builder()
                    .chatId(this.generateChatId(pool))
                    .medias(
                            Stream.concat(
                                    first.stream(),
                                    this.medias.subList(1, this.medias.size()).stream()
                                            .flatMap(m -> m.createInputMedia(this, pool).stream())
                            ).collect(Collectors.toList())
                    )
                    .disableNotification( generateNullableProperty(disableNotification, pool))
                    .protectContent( generateNullableProperty(protectContent, pool))
                    .replyToMessageId( generateNullableProperty(replyTo, pool))
                    .build();
        }

        return SendMessage.builder()
                .chatId(this.generateChatId(pool))
                .text(text.getText().generate(pool))
                .disableWebPagePreview( generateNullableProperty(disableWebPagePreview, pool))
                .disableNotification( generateNullableProperty(disableNotification, pool))
                .protectContent( generateNullableProperty(protectContent, pool))
                .replyToMessageId( generateNullableProperty(replyTo, pool))
                .parseMode(text.getParseMode().generate(pool))
                .build();
    }
}