package org.telegram.telegrise.core.elements.actions;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.Text;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.ElementField;
import org.telegram.telegrise.core.parser.InnerElement;

@Element(name = "send")
@Data @NoArgsConstructor
public class Send implements ActionElement{
    @ElementField(name = "chat", nullable = false, expression = true)
    private GeneratedValue<Long> chatId;
    @InnerElement(nullable = false)
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

    //TODO inner element of <keyboard> ; add other fields
    private GeneratedValue<ReplyKeyboard> replyMarkup;

    @Override
    public BotApiMethod<?> generateMethod(ResourcePool pool) {
        return SendMessage.builder()
                .chatId(chatId.generate(pool))
                .text(text.getText().generate(pool))
                .disableWebPagePreview(disableWebPagePreview != null ? disableWebPagePreview.generate(pool) : null)
                .disableNotification(disableNotification != null ? disableNotification.generate(pool) : null)
                .protectContent(protectContent != null ? protectContent.generate(pool) : null)
                .replyToMessageId(replyTo != null ? replyTo.generate(pool) : null)
                .parseMode(text.getParseMode().generate(pool))
                .build();
    }
}