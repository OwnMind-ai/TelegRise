package org.telegram.telegrise.core.elements.security;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrise.core.elements.TranscriptionElement;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.InnerElement;

import java.util.List;

@Element(name = "roles")
@Data @NoArgsConstructor
public class Roles implements TranscriptionElement {
    @InnerElement(nullable = false)
    private List<Role> roles;
}
