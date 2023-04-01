package org.telegram.telegrise;

import org.telegram.telegrambots.meta.api.objects.User;

public interface RoleProvider {
    String getRole(User user, SessionMemory sessionMemory);
}
