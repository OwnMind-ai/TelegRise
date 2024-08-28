package org.telegram.telegrise.core.elements;

import org.telegram.telegrise.core.elements.actions.ActionElement;
import org.telegram.telegrise.core.parser.TranscriptionMemory;

import java.util.List;

public interface BranchingElement extends StorableElement{
    List<ActionElement> getActions();

    String getName();
    String[] getChatTypes();

    int getLevel();
    void setLevel(int value);
    List<? extends BranchingElement> getChildren();

    @Override
    default void store(TranscriptionMemory memory) {
        memory.put(this.getName(), this);
    }
}
