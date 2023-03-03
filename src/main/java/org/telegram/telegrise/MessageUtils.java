package org.telegram.telegrise;

import org.telegram.telegrambots.meta.api.objects.Message;

public class MessageUtils {
    public static boolean hasMedia(Message message){
        return message != null &&
                (message.hasPhoto() || message.hasVideo() || message.hasDocument() || message.hasAudio() || message.hasAnimation());
    }
}
