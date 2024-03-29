package org.telegram.telegrise;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.commands.scope.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class ChatTypes {
    public static final String ALL = "all";
    public static final String PRIVATE = "private";
    public static final String GROUP = "group";
    public static final String SUPERGROUP = "supergroup";
    public static final String CHANNEL = "channel";

    public static final String DEFAULT_SCOPE = "default";

    public static final List<BotCommandScope> GENERAL_SCOPES_LIST = List.of(
            new BotCommandScopeDefault(), new BotCommandScopeAllPrivateChats(),
            new BotCommandScopeAllGroupChats(), new BotCommandScopeAllChatAdministrators()
    );

    public static boolean isApplicable(List<String> types, Chat chat){
        return chat != null && (types.contains(ALL) || types.contains(chat.getType().trim().toLowerCase()));
    }

    public static boolean isApplicable(List<String> scopes, BotCommandScope scope){
        return scope != null && (scopes.contains("default") || scopes.contains(scope.getType()));
    }

    public static List<String> chatTypesToScopes(String[] chatTypes){
        return Arrays.stream(chatTypes).map(type -> {
            switch (type){
                case ALL: return DEFAULT_SCOPE;
                case PRIVATE: return new BotCommandScopeAllPrivateChats().getType();
                case SUPERGROUP:
                case GROUP: return new BotCommandScopeAllGroupChats().getType();
                default: return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
