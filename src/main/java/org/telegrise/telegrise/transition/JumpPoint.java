package org.telegrise.telegrise.transition;

import org.telegrise.telegrise.core.elements.BranchingElement;
import org.telegrise.telegrise.core.elements.Transition;
import org.telegrise.telegrise.core.elements.actions.ActionElement;

import java.util.List;

public record JumpPoint(BranchingElement from, BranchingElement to, List<ActionElement> actions,
                        Transition nextTransition) {
}
