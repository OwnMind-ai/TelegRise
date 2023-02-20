package org.telegram.telegrise.core.elements;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.ElementField;
import org.telegram.telegrise.core.parser.InnerElement;

import java.util.List;

@Element(name = "tree")
@Data
@NoArgsConstructor
public class Tree implements TranscriptionElement{
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
    @ElementField(name = "predicate", expression = true)
    private GeneratedValue<Boolean> predicate;

    @ElementField(name = "handler")
    private String handlerName;
    private Class<?> handler;

    @InnerElement
    private Text text;
    @InnerElement
    private List<Branch> branches;
    @InnerElement
    private List<Menu> menus;

}
