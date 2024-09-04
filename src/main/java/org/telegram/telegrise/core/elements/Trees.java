package org.telegram.telegrise.core.elements;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.InnerElement;

import java.util.List;
import java.util.function.Consumer;

@EqualsAndHashCode(callSuper = false)
@Element(name = "trees")
@Data @NoArgsConstructor
public class Trees extends NodeElement implements LinkableElement{
    @InnerElement(nullable = false)
    private List<Tree> trees;

    @Override
    public Consumer<BotTranscription> afterParsedTask() {
        return transcription -> transcription.getRoot().getTrees().addAll(trees);
    }
}
