package org.telegrise.telegrise.core.transition;

import org.telegrise.telegrise.core.elements.Transition;
import org.telegrise.telegrise.core.elements.actions.ActionElement;
import org.telegrise.telegrise.core.elements.base.BranchingElement;

import java.util.List;

public record JumpPoint(BranchingElement from, BranchingElement to, List<ActionElement> actions,
                        Transition nextTransition) {
}
