package org.telegram.telegrise.core.elements;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.ElementField;
import org.telegram.telegrise.core.parser.InnerElement;

import java.util.List;

@Element(name = "menu")
@NoArgsConstructor
public class Menu implements TranscriptionElement{
    @Getter
    @ElementField(name = "name", nullable = false)
    private String name;
    @ElementField(name = "type")
    private String type;

    @InnerElement(nullable = false)
    private List<Tree> trees;

    public Menu(String name, String type) {
        this.name = name;
        this.type = type;
    }
}
