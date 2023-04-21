package org.telegram.telegrise;

import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.User;

import java.io.Serializable;
import java.util.Objects;


public final class UserIdentifier implements Serializable {
    public static UserIdentifier of(User user){
        return new UserIdentifier(user.getId());
    }

    @Getter
    private final Long id;

    private UserIdentifier(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserIdentifier that = (UserIdentifier) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
