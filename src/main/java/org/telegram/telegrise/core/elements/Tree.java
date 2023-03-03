package org.telegram.telegrise.core.elements;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrise.MessageUtils;
import org.telegram.telegrise.core.*;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.ElementField;
import org.telegram.telegrise.core.parser.InnerElement;
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.List;

@Element(name = "tree")
@Data
@NoArgsConstructor
public class Tree implements BranchingElement{
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

    //TODO: only static method, static expressions or methods from parent
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
    private LocalNamespace extractHandler(Node node, LocalNamespace namespace){
        if (node.getAttributes().getNamedItem("handler") != null)
            this.handler = namespace.getApplicationNamespace().getClass(node.getAttributes().getNamedItem("handler").getNodeValue());

        return this.createNamespace(namespace.getApplicationNamespace());
    }

    @Override
    public LocalNamespace createNamespace(ApplicationNamespace global) {
        return handler == null ? null : new LocalNamespace(handler, global);
    }

    public boolean canHandle(ResourcePool pool){
        Update update = pool.getUpdate();

        if (this.predicate != null && this.predicate.generate(pool)) {
            return true;
        } else if (this.callbackTriggers != null && update.hasCallbackQuery() && update.getCallbackQuery().getData() != null){
            return Arrays.stream(this.getCallbackTriggers()).anyMatch(c -> c.equals(update.getCallbackQuery().getData()));
        } else if (update.hasMessage() && !MessageUtils.hasMedia(update.getMessage()) && update.getMessage().getText() != null) {
            boolean isCommand = update.getMessage().getText().startsWith(Syntax.COMMAND_START);

            if (isCommand && this.commands != null)
                return Arrays.stream(this.commands)
                        .anyMatch(c -> c.equals(update.getMessage().getText()));
            else if (this.keys != null)
                return Arrays.stream(this.keys)
                        .anyMatch(c -> c.equals(update.getMessage().getText()));

            return false;
        }

        return false;
    }
}
