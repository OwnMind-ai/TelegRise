package org.telegram.telegrise.core.elements;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.InnerElement;

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
