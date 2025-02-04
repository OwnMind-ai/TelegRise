package org.telegrise.telegrise.core.elements.base;

import org.telegrise.telegrise.core.elements.actions.ActionElement;

import java.util.List;

public interface BranchingElement extends StorableElement {
    List<ActionElement> getActions();

    String getName();
    String[] getChatTypes();

    int getLevel();
    void setLevel(int value);
    List<? extends BranchingElement> getChildren();
}
