package org.telegram.telegrise.core.elements;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.ElementField;
import org.telegram.telegrise.core.parser.InnerElement;

import java.util.List;
import java.util.function.Predicate;

@Element(name = "tree")
@NoArgsConstructor
public class Tree implements TranscriptionElement{
    @Getter
    @ElementField(name = "name", nullable = false)
    private String name;
    @ElementField(name = "type")
    private String type;

    @ElementField(name = "commands")
    private String[] commands;
    @ElementField(name = "keys")
    private String[] keys;
    @ElementField(name = "callbackTriggers")
    private String[] callbackTriggers;
    @ElementField(name = "predicate")
    private Predicate<Update> predicate;

    @ElementField(name = "handler")
    private Class<?> handler;

    @InnerElement
    private Text text;
    @InnerElement
    private List<Branch> branches;
    @InnerElement
    private List<Menu> menus;

    public Tree(String name, String type) {
        this.name = name;
        this.type = type;
    }
}
