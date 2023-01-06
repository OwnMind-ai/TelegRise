package org.telegram.telegrise.core.elements;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.telegram.telegrise.core.parser.Element;

import java.util.List;

@Element(name = "menu")
@NoArgsConstructor
public class Menu implements TranscriptionElement{
    @Getter
    private String name;
    private KeyboardType type;

    private List<Tree> trees;

    public Menu(String name, KeyboardType type) {
        this.name = name;
        this.type = type;
    }
}
