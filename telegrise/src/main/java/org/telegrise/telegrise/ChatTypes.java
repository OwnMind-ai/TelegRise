package org.telegrise.telegrise;

import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.commands.scope.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
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
        return Arrays.stream(chatTypes).map(type -> switch (type) {
            case ALL -> DEFAULT_SCOPE;
            case PRIVATE -> new BotCommandScopeAllPrivateChats().getType();
            case SUPERGROUP, GROUP -> new BotCommandScopeAllGroupChats().getType();
            default -> null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
