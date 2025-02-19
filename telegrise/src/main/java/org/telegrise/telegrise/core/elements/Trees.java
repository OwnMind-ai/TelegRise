package org.telegrise.telegrise.core.elements;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegrise.telegrise.core.elements.base.LinkableElement;
import org.telegrise.telegrise.core.elements.base.NodeElement;
import org.telegrise.telegrise.core.elements.head.Link;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.core.parser.InnerElement;

import java.util.List;
import java.util.function.Consumer;

/**
 * An element that stores trees that can be linked using {@code <link>}. All linked trees will be attached to a root.
 *
 * @since 0.1
 * @see Tree
 * @see Link
 */
@Element(name = "trees")
@Setter @Getter @NoArgsConstructor
public class Trees extends NodeElement implements LinkableElement {
    @InnerElement(nullable = false)
    private List<Tree> trees;

    @Override
    public Consumer<BotTranscription> afterParsedTask() {
        return transcription -> transcription.getRoot().getTrees().addAll(trees);
    }
}
