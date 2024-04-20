package org.telegram.telegrise;

import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.User;

import java.io.Serializable;
import java.util.Objects;


@Getter
public final class UserIdentifier implements Serializable {
    public static UserIdentifier of(User user){
        return new UserIdentifier(user.getId(), user.getLanguageCode());
    }
    public static UserIdentifier ofId(Long id){
        return new UserIdentifier(id);
    }

    private final Long id;
    @Setter
    private String languageCode;

    private UserIdentifier(Long id) {
        this.id = id;
    }
    private UserIdentifier(Long id, String languageCode) {
        this.id = id;
        this.languageCode = languageCode;
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
