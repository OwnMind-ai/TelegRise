package org.telegrise.telegrise.builtin;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegrise.telegrise.annotations.Reference;
import org.telegrise.telegrise.annotations.ReferenceGenerator;
import org.telegrise.telegrise.annotations.TreeController;
import org.telegrise.telegrise.core.elements.base.NodeElement;
import org.telegrise.telegrise.generators.GeneratedReference;
import org.telegrise.telegrise.utils.MessageUtils;
import org.w3c.dom.Node;

/**
 * Builtin implementation of tree controller, which provides basic method references to be used.
 * It can be used as a super class for custom trees or just by itself.
 *
 * @see TreeController
 * @since 0.8
 */
@Slf4j
@TreeController
public class DefaultController {  //TODO consider making Update a @HiddenParameter, might prove useful
    /**
     * Logs error messages that can be obtained from {@code onError} attribute of action elements.
     */
    @Reference
    public void logError(TelegramApiException e, Node node){
        log.error("An error occurred when executing: {}", NodeElement.formatNode(node), e);
    }

    @Reference
    public void ignore(){}

    /**
     * Concatenates an array of strings or array of object's {@code ::toString}.
     * @param parts objects or strings to concatenate
     * @return result string
     */
    @Reference
    public String concat(Object... parts){
        StringBuilder sb = new StringBuilder();
        for (Object part : parts) {
            String string = part.toString();
            sb.append(string);
        }
        return sb.toString();
    }

    /**
     * @param update current update
     * @return true if update has a {@code CallbackQuery}
     */
    @Reference
    public boolean isCallback(Update update){
        return update.hasCallbackQuery();
    }

    /**
     * Produces {@link GeneratedReference} that returns {@code true}
     * if passed update has a {@code CallbackQuery} and its data equals {@code data} parameter.
     * <pre>
     * {@code
     * <branch when='::callback("data")'>
     * }
     *
     * @param data expected data of a callback query
     * @return generated reference
     */
    @ReferenceGenerator
    public GeneratedReference<Update, Boolean> callback(String data){
        return u -> u.hasCallbackQuery() && u.getCallbackQuery().getData().equals(data);
    }

    /**
     * Produces {@link GeneratedReference} that returns {@code true}
     * if passed update has a {@code CallbackQuery} and its data matches {@code regex} parameter.
     * <pre>
     * {@code
     * <branch when='::callbackMatches("\\d+")'>
     * }
     *
     * @param regex a pattern to be matched with
     * @return generated reference
     */
    @ReferenceGenerator
    public GeneratedReference<Update, Boolean> callbackMatches(String regex){
        return u -> u.hasCallbackQuery() && u.getCallbackQuery().getData().matches(regex);
    }

    @Reference
    public Integer getMessageId(Update update){
        return update == null ? null : MessageUtils.getMessageId(update);
    }

    /**
     * @param update current update
     * @return true if update has a {@code Message}
     */
    @Reference
    public boolean isMessage(Update update){
        return update.hasMessage();
    }

    /**
     * @param update current update
     * @return true if update has a {@code Message} and the message has text
     */
    @Reference
    public boolean isTextMessage(Update update){
        return update.hasMessage() && update.getMessage().hasText();
    }

    /**
     * @param update current update
     * @return true if update has a {@code Message} and the message has caption
     */
    @Reference
    public boolean isCaptionMessage(Update update){
        return update.hasMessage() && update.getMessage().hasCaption();
    }

    /**
     * Produces {@link GeneratedReference} that returns {@code true}
     * if passed update has a {@code Message} and its text (if exists) equals {@code text} parameter.
     * <pre>
     * {@code
     * <branch when='::messageText("text")'>
     * }
     *
     * @param text expected text of the message
     * @return generated reference
     */
    @ReferenceGenerator
    public GeneratedReference<Update, Boolean> messageText(String text){
        return u -> u.hasMessage() && text.equals(u.getMessage().getText());
    }

    /**
     * Produces {@link GeneratedReference} that returns {@code true}
     * if passed update has a {@code Message} and its text (if exists) matches {@code regex} parameter.
     * <pre>
     * {@code
     * <branch when='::messageTextMatches("\\d+")'>
     * }
     *
     * @param regex a pattern to be matched with
     * @return generated reference
     */
    @ReferenceGenerator
    public GeneratedReference<Update, Boolean> messageTextMatches(String regex){
        return u -> u.hasMessage() && u.getMessage().hasText() && u.getMessage().getText().matches(regex);
    }

    /**
     * Produces {@link GeneratedReference} that returns {@code true}
     * if passed update has a {@code Message} and its caption (if exists) equals {@code text} parameter.
     * <pre>
     * {@code
     * <branch when='::messageCaption("caption")'>
     * }
     *
     * @param text expected caption of the message
     * @return generated reference
     */
    @ReferenceGenerator
    public GeneratedReference<Update, Boolean> messageCaption(String text){
        return u -> u.hasMessage() && text.equals(u.getMessage().getCaption());
    }

    /**
     * Produces {@link GeneratedReference} that returns {@code true}
     * if passed update has a {@code Message} and its caption (if exists) matches {@code regex} parameter.
     * <pre>
     * {@code
     * <branch when='::messageCaptionMatches("\\d+")'>
     * }
     *
     * @param regex a pattern to be matched with
     * @return generated reference
     */
    @ReferenceGenerator
    public GeneratedReference<Update, Boolean> messageCaptionMatches(String regex){
        return u -> u.hasMessage() && u.getMessage().hasCaption() && u.getMessage().getCaption().matches(regex);
    }

    /**
     * Produces {@link GeneratedReference} that returns {@code true}
     * if passed update has a {@code Message} and its text or caption (if exists) equal {@code text} parameter.
     * <pre>
     * {@code
     * <branch when='::messageTextOrCaption("caption")'>
     * }
     *
     * @param text expected text or caption of the message
     * @return generated reference
     */
    @ReferenceGenerator
    public GeneratedReference<Update, Boolean> messageTextOrCaption(String text){
        return u -> u.hasMessage() && (text.equals(u.getMessage().getText()) || text.equals(u.getMessage().getCaption()));
    }

    /**
     * Produces {@link GeneratedReference} that returns {@code true}
     * if passed update has a {@code Message} and its text or caption
     * (if exists) match {@code regex} parameter.
     * <pre>
     * {@code
     * <branch when='::messageTextOrCaptionMatches("\\d+")'>
     * }
     *
     * @param regex a pattern to be matched with
     * @return generated reference
     */
    @ReferenceGenerator
    public GeneratedReference<Update, Boolean> messageTextOrCaptionMatches(String regex){
        return u -> u.hasMessage() && ((u.getMessage().hasText() && u.getMessage().getText().matches(regex))
                || (u.getMessage().hasCaption() && u.getMessage().getCaption().matches(regex)));
    }

    /**
     * @return {@code ReplyKeyboardRemove} instance
     */
    @Reference
    public static ReplyKeyboard replyKeyboardRemove(){
        return new ReplyKeyboardRemove(true);
    }
}
