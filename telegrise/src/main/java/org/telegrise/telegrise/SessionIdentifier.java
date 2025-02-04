package org.telegrise.telegrise;

import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;

import java.io.Serializable;
import java.util.Objects;


@Getter
public final class SessionIdentifier implements Serializable {
    public static SessionIdentifier of(User user, Chat chatId){
        return new SessionIdentifier(user.getId(), chatId.getId(), user.getLanguageCode());
    }
    public static SessionIdentifier of(Long user, Long chat) { return new SessionIdentifier(user, chat); }
    public static SessionIdentifier ofUserOnly(User user) { return new SessionIdentifier(user.getId(), null); }
    public static SessionIdentifier ofUserOnly(Long id) { return new SessionIdentifier(id, null); }

    public static final String SESSION_CHAT = "chat";
    public static final String SESSION_USER = "user";

    private final Long userId;
    private final Long chatId;

    @Setter
    private String languageCode;   //TODO move to session memory

    private SessionIdentifier(Long userId, Long chatId) {
        this.userId = userId;
        this.chatId = chatId;
    }
    private SessionIdentifier(Long userId, Long chatId, String languageCode) {
        this.chatId = chatId;
        this.userId = userId;
        this.languageCode = languageCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SessionIdentifier that = (SessionIdentifier) o;
        return Objects.equals(userId, that.userId) && Objects.equals(chatId, that.chatId);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(userId);
        result = 31 * result + Objects.hashCode(chatId);
        return result;
    }

    @Override
    public String toString() {
        return "SessionIdentifier{" +
                "userId=" + userId +
                ", chatId=" + chatId +
                ", languageCode='" + languageCode + '\'' +
                '}';
    }
}
