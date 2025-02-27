package org.telegrise.telegrise.types;

import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.User;

@Getter
public class BotUser {
    private final User user;

    public BotUser(User user) {
        this.user = user;
    }

    public Long getId() {
        return user.getId();
    }
    
    public String getUsername(){
        return user.getUserName();
    }
}
