package org.telegram.telegrise;

import java.util.List;

public interface SessionInitializer{
    void initialize(SessionMemory memory);

    default List<SessionIdentifier> getInitializionList(){ return List.of(); }
}
