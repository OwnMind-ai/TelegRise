package org.telegram.telegrise.transition;

import org.telegram.telegrise.core.elements.BranchingElement;
import org.telegram.telegrise.core.elements.Transition;
import org.telegram.telegrise.core.elements.actions.ActionElement;

import java.util.List;

public record JumpPoint(BranchingElement from, BranchingElement to, List<ActionElement> actions,
                        Transition nextTransition) {
}
