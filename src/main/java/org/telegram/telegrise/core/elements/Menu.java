package org.telegram.telegrise.core.elements;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrise.MessageUtils;
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
        Chat chat = MessageUtils.getChat(pool.getUpdate());

        Tree founded = trees.stream()
                .filter(t -> t.isChatApplicable(chatTypes, chat))
                .filter(t -> t.canHandleMessage(pool)).findFirst().orElse(null);

        if (founded == null){
            for (Tree tree : trees) {
                if (tree.getPredicate() != null && tree.isChatApplicable(chatTypes, chat) && tree.getPredicate().generate(pool)) {
                        founded = tree;
                        break;
                }
            }
        }

        return founded;
    }
}
