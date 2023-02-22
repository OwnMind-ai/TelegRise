package org.telegram.telegrise.core.elements;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrise.core.ApplicationNamespace;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.LocalNamespace;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.ElementField;
import org.telegram.telegrise.core.parser.InnerElement;
import org.w3c.dom.Node;

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
    private Class<?> handler;

    @InnerElement
    private Text text;
    @InnerElement
    private List<Branch> branches;
    @InnerElement
    private List<Menu> menus;

    @ElementField(priority = Double.POSITIVE_INFINITY)
    private LocalNamespace extractHandler(Node node, ApplicationNamespace namespace){
        if (node.getAttributes().getNamedItem("handler") != null)
            this.handler = namespace.getClass(node.getAttributes().getNamedItem("handler").getNodeValue());

        return this.createNamespace(namespace);
    }

    @Override
    public LocalNamespace createNamespace(ApplicationNamespace global) {
        return handler == null ? null : new LocalNamespace(handler, global);
    }
}
