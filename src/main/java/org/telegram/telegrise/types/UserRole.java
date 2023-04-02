package org.telegram.telegrise.types;

import lombok.Data;
import org.telegram.telegrise.core.elements.security.Role;

import java.io.Serializable;

@Data
public class UserRole implements Serializable {
    public static UserRole ofRole(Role role){
        return new UserRole(role.getName(), role.getTrees(), role.getLevel());
    }

    private final String name;
    private final String[] trees;
    private final Integer level;
}
