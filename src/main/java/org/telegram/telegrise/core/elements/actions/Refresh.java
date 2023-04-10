package org.telegram.telegrise.core.elements.actions;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrise.MessageUtils;
import org.telegram.telegrise.TelegRiseRuntimeException;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.keyboard.Keyboard;
import org.telegram.telegrise.core.elements.text.Text;
import org.telegram.telegrise.core.parser.*;
import org.telegram.telegrise.keyboard.DynamicKeyboard;
import org.w3c.dom.Node;

import java.util.List;

@Element(name = "refresh")
@Data @NoArgsConstructor
public class Refresh implements ActionElement {
    public static final String CALLBACK = "callback";
    public static final String LAST = "last";

    @Attribute(name = "type")
    private String type = LAST;

    @Attribute(name = "keyboardId")
    private String keyboardId;

    @InnerElement
    private Text text;

    @InnerElement
    private Keyboard keyboard;

    @Override
    public void validate(Node node, TranscriptionMemory memory) {
        if (!LAST.equals(type) && !CALLBACK.equals(type))
            throw new TranscriptionParsingException("Invalid refresh type '" + type + "', possible types are: '"
                    + LAST + "' or '" + CALLBACK + "'" , node);

        if (text == null && keyboardId == null && keyboard == null)
            throw new TranscriptionParsingException("No keyboard or text specified", node);

        if (keyboard != null && keyboardId != null)
            throw new TranscriptionParsingException("KeyboardId and keyboard element conflict with each other", node);
    }

    @Override
    public PartialBotApiMethod<?> generateMethod(ResourcePool pool) {
        Message target = this.extractMessageTarget(pool);

        InlineKeyboardMarkup markup = this.getKeyboardId() != null ?
                pool.getMemory().get(this.getKeyboardId(), DynamicKeyboard.class).createInline(pool)
                :  this.getKeyboard() != null ? (InlineKeyboardMarkup) this.getKeyboard().createMarkup(pool) : null;

        if (this.getText() == null && markup != null){
            return EditMessageReplyMarkup.builder()
                    .chatId(target.getChatId())
                    .messageId(target.getMessageId())
                    .replyMarkup(markup)
                    .build();
        } else if (this.getText() != null){
            if (MessageUtils.hasMedia(target))
                return EditMessageCaption.builder()
                        .chatId(target.getChatId())
                        .messageId(target.getMessageId())
                        .caption(text.generateText(pool))
                        .captionEntities(text.getEntities() != null ? text.getEntities().generate(pool) : List.of())
                        .parseMode(text.getParseMode() != null ? text.getParseMode().generate(pool) : null)
                        .replyMarkup(markup)
                        .build();
            else
                return EditMessageText.builder()
                        .chatId(target.getChatId())
                        .messageId(target.getMessageId())
                        .text(text.generateText(pool))
                        .entities(text.getEntities() != null ? text.getEntities().generate(pool) : List.of())
                        .parseMode(text.getParseMode() != null ? text.getParseMode().generate(pool) : null)
                        .replyMarkup(markup)
                        .build();
        } else
            throw new TelegRiseRuntimeException("Nothing to refresh");
    }

    private Message extractMessageTarget(ResourcePool pool) {
        if (Refresh.LAST.equals(this.getType())){
            if (pool.getMemory().getLastSentMessage() == null)
                throw new TelegRiseRuntimeException("Unable to apply refresh element: last sent message doesn't exists");

            return pool.getMemory().getLastSentMessage();
        } else if (Refresh.CALLBACK.equals(this.getType())) {
            if (pool.getUpdate() == null || !pool.getUpdate().hasCallbackQuery())
                throw new TelegRiseRuntimeException("Unable to apply refresh element: passed update has no callback query");

            return pool.getUpdate().getCallbackQuery().getMessage();
        }

        throw new TelegRiseRuntimeException("Unable to apply refresh element: unknown refresh type " + this.getType());
    }

    @Override
    public GeneratedValue<Long> getChatId() {
        return null;
    }
}
