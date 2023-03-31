package org.telegram.telegrise;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtils {
    private static final Pattern COMMAND_PATTERN = Pattern.compile("^/(\\w*)(?>@.+)?$");

    public static boolean hasMedia(Message message){
        return message != null &&
                (message.hasPhoto() || message.hasVideo() || message.hasDocument() || message.hasAudio() || message.hasAnimation());
    }

    public static User getFrom(Update update){
        return update.hasMessage() ? update.getMessage().getFrom()
                : update.hasCallbackQuery() ? update.getCallbackQuery().getFrom()
                : update.hasEditedMessage() ? update.getEditedMessage().getFrom()
                : update.hasChannelPost() ? update.getChannelPost().getFrom()
                : update.hasMyChatMember() ? update.getMyChatMember().getFrom()
                : update.hasChatMember() ? update.getChatMember().getFrom()
                : update.hasEditedChannelPost() ? update.getEditedChannelPost().getFrom()
                : update.hasChatJoinRequest() ? update.getChatJoinRequest().getUser()
                : update.hasChosenInlineQuery() ? update.getChosenInlineQuery().getFrom()
                : update.hasInlineQuery() ? update.getInlineQuery().getFrom()
                : update.hasPollAnswer() ? update.getPollAnswer().getUser()
                : update.hasShippingQuery() ? update.getShippingQuery().getFrom()
                : update.hasPreCheckoutQuery() ? update.getPreCheckoutQuery().getFrom()
                : null;
    }

    public static Chat getChat(Update update) {
        return update.hasMessage() ? update.getMessage().getChat()
                : update.hasCallbackQuery() ? update.getCallbackQuery().getMessage().getChat()
                : update.hasEditedMessage() ? update.getEditedMessage().getChat()
                : update.hasChannelPost() ? update.getChannelPost().getChat()
                : update.hasMyChatMember() ? update.getMyChatMember().getChat()
                : update.hasChatMember() ? update.getChatMember().getChat()
                : update.hasEditedChannelPost() ? update.getEditedChannelPost().getChat()
                : update.hasChatJoinRequest() ? update.getChatJoinRequest().getChat()
                : null;
    }

    public static boolean isCommand(String raw){
        return COMMAND_PATTERN.matcher(raw).matches();
    }

    public static String cleanCommand(String raw){
        Matcher matcher = COMMAND_PATTERN.matcher(raw);
        if (!matcher.find()) throw new UnsupportedOperationException();

        return matcher.group(1);
    }
}
