package org.telegram.telegrise.core.elements.actions;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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
            //TODO send mediagroup
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