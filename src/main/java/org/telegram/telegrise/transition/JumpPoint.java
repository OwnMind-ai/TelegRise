package org.telegram.telegrise.transition;

import lombok.Data;
import org.telegram.telegrise.core.elements.BranchingElement;
import org.telegram.telegrise.core.elements.Transition;
import org.telegram.telegrise.core.elements.actions.ActionElement;

import java.util.List;

@Data
public final class JumpPoint {
    private final BranchingElement from;
    private final BranchingElement to;

    private final List<ActionElement> actions;
    private final Transition nextTransition;
}
