package org.telegrise.telegrise.core.elements.security;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegrise.telegrise.RoleProvider;
import org.telegrise.telegrise.core.elements.base.NodeElement;
import org.telegrise.telegrise.core.elements.base.StorableElement;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.core.parser.TranscriptionMemory;
import org.telegrise.telegrise.exceptions.TranscriptionParsingException;

/**
 * Represents a role that a user can be assigned, determining parts of the application they can or cannot access.
 * <p>
 * When at least one role is defined, the application will use provided {@link RoleProvider} to assign roles to users.
 * Roles can have specified {@code trees} that users are allowed to use and {@code accessLevel}.
 * Users of this role will not be able to access trees that have an access level greater than this role's access level.
 * If defined {@code onDeniedTree}, the user's request will be rerouted to the specified tree in case of rejection.
 * <p>
 * For a {@link RoleProvider} to assign a role to a user, it must return a name of one of the defined roles.
 * <pre>
 * {@code
 * <roles>
 *     <role name="admin" accessLevel="100"/>
 *     <role name="user" accessLevel="0" onDeniedTree="DeniedTree"/>
 * </roles>
 * }
 * </pre>
 *
 * @see RoleProvider
 * @since 0.1
 */
@Element(name = "role")
@Getter @Setter @NoArgsConstructor
public class Role extends NodeElement implements StorableElement {
    /**
     * Name of this role
     */
    @Attribute(name = "name", nullable = false)
    private String name;

    /**
     * List of trees that this role can access
     */
    @Attribute(name = "accessibleTrees")
    private String[] trees;

    /**
     * Access level of this role
     */
    @Attribute(name = "level")
    private Integer level;

    /**
     * A tree to which users of this role will be rerouted on rejection
     */
    @Attribute(name = "onDeniedTree")
    private String onDeniedTree;

    @Override
    public void validate(TranscriptionMemory memory) {
        if (level == null && trees == null)
            throw new TranscriptionParsingException("Either 'accessibleTrees' or 'level' must be specified", node);
    }

    @Override
    public void store(TranscriptionMemory memory) {
        memory.put(parentTree, this.name, this);
    }
}
