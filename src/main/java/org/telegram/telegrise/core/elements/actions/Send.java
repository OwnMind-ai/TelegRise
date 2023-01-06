package org.telegram.telegrise.core.elements.actions;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.elements.Text;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.ElementField;
import org.telegram.telegrise.core.parser.InnerElement;

@Element(name = "send")
@Data @NoArgsConstructor
public class Send implements ActionElement{
    @ElementField(name = "chatId", nullable = false, expression = true)
    private GeneratedValue<Long> chatId;
    @InnerElement(nullable = false)
    private Text text;

    @ElementField(name = "disableWebPreview", expression = true)
    private GeneratedValue<Boolean> disableWebPreview;
    @ElementField(name = "disableNotification", expression = true)
    private GeneratedValue<Boolean> disableNotification;
    @ElementField(name = "protectContent", expression = true)
    private GeneratedValue<Boolean> protectContent;
    @ElementField(name = "allowSendingWithoutReply", expression = true)
    private GeneratedValue<Boolean> allowSendingWithoutReply;
    @ElementField(name = "replyTo", expression = true)
    private GeneratedValue<Integer> replyTo;

    //TODO inner element of <keyboard>
    private GeneratedValue<ReplyKeyboard> replyMarkup;

    @Override
    public PartialBotApiMethod<?> generateMethod() {
        return null;
    }
}