package org.telegram.telegrise;

import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.media.*;
import org.telegram.telegrise.types.CommandData;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtils {
    private static final Pattern COMMAND_PATTERN = Pattern.compile("^/(?<name>\\w*)(?>@(?<username>.+))?$");

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

    public static CommandData parseCommand(String command){
        Matcher matcher = COMMAND_PATTERN.matcher(command);
        if (!matcher.find()) return null;

        return new CommandData(matcher.group("name"), matcher.group("username"));
    }

    public static InputMedia toInputMedia(Message message){
        if (!hasMedia(message)) return null;

        InputMedia result;

        if (message.hasPhoto()){
            result = InputMediaPhoto.builder()
                    .media(message.getPhoto().get(message.getPhoto().size() - 1).getFileId())
                    .hasSpoiler(message.getHasMediaSpoiler())
                    .build();
        } else if (message.hasVideo()) {
            result = InputMediaVideo.builder()
                    .media(message.getVideo().getFileId())
                    .width(message.getVideo().getWidth())
                    .height(message.getVideo().getHeight())
                    .duration(message.getVideo().getDuration())
                    .thumb(new InputFile(message.getVideo().getThumb().getFileId()))
                    .hasSpoiler(message.getHasMediaSpoiler())
                    .build();
        } else if (message.hasAudio()) {
            result = InputMediaAudio.builder()
                    .media(message.getAudio().getFileId())
                    .duration(message.getAudio().getDuration())
                    .performer(message.getAudio().getPerformer())
                    .title(message.getAudio().getTitle())
                    .thumb(new InputFile(message.getAudio().getThumb().getFileId()))
                    .build();
        } else if (message.hasDocument()) {
            result = InputMediaDocument.builder()
                    .media(message.getDocument().getFileId())
                    .thumb(new InputFile(message.getDocument().getThumb().getFileId()))
                    .build();
        } else if (message.hasAnimation()) {
            result = InputMediaAnimation.builder()
                    .media(message.getAnimation().getFileId())
                    .width(message.getAnimation().getWidth())
                    .height(message.getAnimation().getHeight())
                    .duration(message.getAnimation().getDuration())
                    .thumb(new InputFile(message.getAnimation().getThumb().getFileId()))
                    .build();
        } else
            return null;

        result.setCaption(message.getCaption());
        result.setCaptionEntities(message.getCaptionEntities());

        return result;
    }
}
