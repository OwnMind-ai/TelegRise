package org.telegrise.telegrise.types;

import org.jetbrains.annotations.ApiStatus;
import org.telegrise.telegrise.core.elements.security.Role;

import java.io.Serializable;

/**
 * An object that contains general information about the user's role and their access.
 * @param name name of the role
 * @param trees list of trees that user can access
 * @param level access level
 * @param onDeniedTree tree to be used for handling denied requests
 */
public record UserRole(String name, String[] trees, Integer level, String onDeniedTree) implements Serializable {
    @ApiStatus.Internal
    public UserRole {}

    @ApiStatus.Internal
    public static UserRole ofRole(Role role) {
        return new UserRole(role.getName(), role.getTrees(), role.getLevel(), role.getOnDeniedTree());
    }
}