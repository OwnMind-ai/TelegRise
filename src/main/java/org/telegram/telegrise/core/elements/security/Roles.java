package org.telegram.telegrise.core.elements.security;

import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.telegram.telegrise.core.elements.NodeElement;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.InnerElement;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Element(name = "roles")
@Getter @Setter @NoArgsConstructor
public class Roles extends NodeElement {
    @InnerElement(nullable = false)
    private List<Role> roles;
}
