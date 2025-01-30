package org.telegrise.telegrise.core.elements.security;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegrise.telegrise.core.elements.NodeElement;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.core.parser.InnerElement;

import java.util.List;

@Element(name = "roles")
@Getter @Setter @NoArgsConstructor
public class Roles extends NodeElement {
    @InnerElement(nullable = false)
    private List<Role> roles;
}
