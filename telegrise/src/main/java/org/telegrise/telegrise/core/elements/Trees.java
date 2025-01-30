package org.telegrise.telegrise.core.elements;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.core.parser.InnerElement;

import java.util.List;
import java.util.function.Consumer;

@Element(name = "trees")
@Setter @Getter @NoArgsConstructor
public class Trees extends NodeElement implements LinkableElement{
    @InnerElement(nullable = false)
    private List<Tree> trees;

    @Override
    public Consumer<BotTranscription> afterParsedTask() {
        return transcription -> transcription.getRoot().getTrees().addAll(trees);
    }
}
