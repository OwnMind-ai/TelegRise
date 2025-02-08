package org.telegrise.telegrise.types;

import org.jetbrains.annotations.ApiStatus;
import org.telegrise.telegrise.core.elements.security.Role;

import java.io.Serializable;

public record UserRole(String name, String[] trees, Integer level, String onDeniedTree) implements Serializable {
    @ApiStatus.Internal
    public UserRole {}

    @ApiStatus.Internal
    public static UserRole ofRole(Role role) {
        return new UserRole(role.getName(), role.getTrees(), role.getLevel(), role.getOnDeniedTree());
    }
}