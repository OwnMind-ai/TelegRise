package org.telegrise.telegrise;

import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;

import java.io.Serializable;
import java.util.Objects;


/**
 * This class contains identifying data for a session, specifically {@code userId} and {@code chatId}.
 * <p>
 * In the case of the 'user' session type, {@code chatId} will always be equal to {@code userId}.
 *
 * @since 0.1
 */
@Getter
public final class SessionIdentifier implements Serializable {
    public static SessionIdentifier of(User user, Chat chatId){
        return new SessionIdentifier(user.getId(), chatId.getId());
    }
    public static SessionIdentifier of(Long user, Long chat) { return new SessionIdentifier(user, chat); }
    public static SessionIdentifier ofUserOnly(User user) { return new SessionIdentifier(user.getId(), user.getId()); }
    public static SessionIdentifier ofUserOnly(Long id) { return new SessionIdentifier(id, id); }

    public static final String SESSION_CHAT = "chat";
    public static final String SESSION_USER = "user";

    private final long userId;
    private final long chatId;

    private SessionIdentifier(long userId, long chatId) {
        this.userId = userId;
        this.chatId = chatId;
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
                '}';
    }
}
