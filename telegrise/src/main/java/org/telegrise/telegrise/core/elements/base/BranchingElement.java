package org.telegrise.telegrise.core.elements.base;

import org.telegrise.telegrise.core.elements.actions.ActionElement;

import java.util.List;

/**
 * Represents an element that is part of the branching system in transcription, such as trees, branches and the root.
 *
 * @see org.telegrise.telegrise.core.elements.Branch
 * @see org.telegrise.telegrise.core.elements.Tree
 * @see org.telegrise.telegrise.core.elements.Root
 * @since 0.1
 */
public interface BranchingElement extends StorableElement {
    List<ActionElement> getActions();

    String getName();
    String[] getChatTypes();

    int getLevel();
    void setLevel(int value);
    List<? extends BranchingElement> getChildren();
}
