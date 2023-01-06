package org.telegram.telegrise.core.elements;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrise.core.parser.Element;

import java.util.List;
import java.util.function.Predicate;

@Element(name = "tree")
@NoArgsConstructor
public class Tree implements TranscriptionElement{
    @Getter
    private String name;
    private KeyboardType type;

    private String[] commands;
    private String[] keys;
    private String[] callbackTriggers;
    private Predicate<Update> predicate;

    private Class<?> handler;
    private Text text;
    private List<Branch> branches;
    private List<Menu> menus;

    public Tree(String name, KeyboardType type) {
        this.name = name;
        this.type = type;
    }
}
