package org.telegram.telegrise.core.elements;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrise.SessionMemoryImpl;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.actions.ActionElement;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.InnerElement;

import java.util.List;

@Element(name = "menu")
@Data
@NoArgsConstructor
public class Menu implements BranchingElement{
    @Attribute(name = "name", nullable = false)
    private String name;
    @Attribute(name = "interruptible")
    private boolean interpretable = true;

    @Attribute(name = "chats")
    private String[] chatTypes;

    @InnerElement(nullable = false)
    private List<Tree> trees;

    @InnerElement
    private List<ActionElement> actions;

    @InnerElement
    private DefaultBranch defaultBranch;

    public Tree findTree(ResourcePool pool, SessionMemoryImpl sessionMemory){
        List<String> chatTypes = List.of(sessionMemory.getLastChatTypes());

        return trees.stream().filter(t -> t.canHandle(pool, chatTypes)).findFirst().orElse(null);
    }
}
