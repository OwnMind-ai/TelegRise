package org.telegrise.telegrise.core.elements;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.SessionMemoryImpl;
import org.telegrise.telegrise.core.elements.actions.ActionElement;
import org.telegrise.telegrise.core.elements.base.BranchingElement;
import org.telegrise.telegrise.core.elements.base.NodeElement;
import org.telegrise.telegrise.core.elements.keyboard.Keyboards;
import org.telegrise.telegrise.core.elements.text.Texts;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.core.parser.InnerElement;
import org.telegrise.telegrise.core.parser.TranscriptionMemory;
import org.telegrise.telegrise.utils.MessageUtils;

import java.util.List;

/**
 * A root element that contains trees of this transcription.
 * <p>
 * This element can have action elements like {@code <send>} but cannot have a controller
 * (if needed, static references must be used).
 * Branching that has no other branches and no transition will lead to this element after handling the update.
 *
 * @since 0.1
 */
@Element(name = "root")
@Getter @Setter
@NoArgsConstructor
public class Root extends NodeElement implements BranchingElement {
    /**
     * Name of the root element to be used in transition target, defaults to {@code "root"}
     */
    @Attribute(name = "name")
    private String name = "root";

    /**
     * List of chat types that this bot can be used in
     */
    @Attribute(name = "chats")
    private String[] chatTypes;

    @InnerElement
    private List<Tree> trees;

    @InnerElement
    private List<ActionElement> actions;

    @InnerElement
    private DefaultBranch defaultBranch;

    @InnerElement
    private Texts texts;

    @InnerElement
    private Keyboards keyboards;

    private int level = -1;

    public Tree findTree(ResourcePool pool, SessionMemoryImpl sessionMemory){
        List<String> chatTypes = List.of(sessionMemory.getLastChatTypes());
        Chat chat = MessageUtils.getChat(pool.getUpdate());

        Tree founded = trees.stream()
                .filter(t -> t.isChatApplicable(chatTypes, chat))
                .filter(t -> t.canHandleMessage(pool, chat)).findFirst().orElse(null);

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

    @Override
    public List<? extends BranchingElement> getChildren() {
        return trees;
    }

    @Override
    public void store(TranscriptionMemory memory) {
        memory.put(parentTree, this.getName(), this);
    }
}
