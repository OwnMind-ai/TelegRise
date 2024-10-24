package org.telegram.telegrise.types;

import org.telegram.telegrise.core.elements.security.Role;

import java.io.Serializable;

public record UserRole(String name, String[] trees, Integer level) implements Serializable {
    public static UserRole ofRole(Role role) {
        return new UserRole(role.getName(), role.getTrees(), role.getLevel());
    }

}
