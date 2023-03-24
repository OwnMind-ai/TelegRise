package org.telegram.telegrise.transition;

import lombok.Data;
import org.telegram.telegrise.core.elements.BranchingElement;

@Data
public final class JumpPoint {
    private final BranchingElement from;
    private final BranchingElement to;
}
