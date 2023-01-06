package org.telegram.telegrise.core.elements.actions;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.elements.Text;
import org.telegram.telegrise.core.parser.Element;

@Element(name = "send")
@Data @NoArgsConstructor
public class Send implements ActionElement{
    private GeneratedValue<Long> chatId;
    private Text text;

    private GeneratedValue<Boolean> disableWebPreview;
    private GeneratedValue<Boolean> disableNotification;
    private GeneratedValue<Boolean> protectContent;
    private GeneratedValue<Boolean> allowSendingWithoutReply;
    private GeneratedValue<Integer> replyTo;

    private GeneratedValue<ReplyKeyboard> replyMarkup;

    @Override
    public PartialBotApiMethod<?> generateMethod() {
        return null;
    }
}