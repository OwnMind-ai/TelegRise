package org.telegrise.telegrise;

/**
 * Represent a class that chooses a role to be assigned to a user.
 * <p>
 * The role of the user is decided when their session is created
 * and can be changed using {@link SessionMemory#setUserRole(String)}.
 * Implementation can use {@link org.telegrise.telegrise.annotations.Resource Resource} annotation to
 * inject the necessary resources.
 *
 * @since 0.1
 */
public interface RoleProvider {
    /**
     * Determines a role for the user. Returned role name must match with one of {@code <role>} elements.
     * @return name of the role
     */
    String getRole(SessionMemory sessionMemory);
}
