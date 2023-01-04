package core.elements;

import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.function.Predicate;

public class Tree implements TranscriptionElement{
    @Getter
    private final String name;
    private final KeyboardType type;

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
