package org.telegram.telegrise;

import java.util.List;

public interface SessionInitializer{
    void initialize(SessionMemory memory);

    default List<UserIdentifier> getInitializionList(){ return List.of(); }
}
