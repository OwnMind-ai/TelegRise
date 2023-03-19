package org.telegram.telegrise;

import org.telegram.telegrambots.meta.api.objects.Chat;

import java.util.List;

public final class ChatTypes {
    public static final String ALL = "all";
    public static final String PRIVATE = "private";
    public static final String GROUP = "group";
    public static final String SUPERGROUP = "supergroup";
    public static final String CHANNEL = "channel";

    private static final List<String> types = List.of(ALL, PRIVATE, GROUP, SUPERGROUP, CHANNEL);

    public static boolean isApplicable(List<String> types, Chat chat){
        return chat != null && (types.contains("all") || types.contains(chat.getType().trim().toLowerCase()));
    }
}
