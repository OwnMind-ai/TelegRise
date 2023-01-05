package org.telegram.telegrise.core.elements;

import lombok.Getter;

import java.util.List;

public class Menu implements TranscriptionElement{
    @Getter
    private final String name;
    private final KeyboardType type;

    private List<Tree> trees;

    public Menu(String name, KeyboardType type) {
        this.name = name;
        this.type = type;
    }
}
