package org.telegram.telegrise.core.elements;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrise.ChatTypes;
import org.telegram.telegrise.MessageUtils;
import org.telegram.telegrise.core.*;
import org.telegram.telegrise.core.elements.actions.ActionElement;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.InnerElement;
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Element(name = "tree")
@Data
@NoArgsConstructor
public class Tree implements BranchingElement{
    @Attribute(name = "name", nullable = false)
    private String name;

    @Attribute(name = "commands")
    private String[] commands;
    @Attribute(name = "keys")
    private String[] keys;
    @Attribute(name = "callbackTriggers")
    private String[] callbackTriggers;
    @Attribute(name = "predicate", expression = true)
    private GeneratedValue<Boolean> predicate;
    @Attribute(name = "chats")
    private String[] chatTypes;
    private Class<?> handler;

    @InnerElement
    private List<ActionElement> actions;
    @InnerElement
    private List<Branch> branches;
    @InnerElement
    private List<Menu> menus;
    @InnerElement
    private DefaultBranch defaultBranch;

    @Attribute(priority = Double.POSITIVE_INFINITY)
    private LocalNamespace extractHandler(Node node, LocalNamespace namespace){
        if (node.getAttributes().getNamedItem("handler") != null)
            this.handler = namespace.getApplicationNamespace().getClass(node.getAttributes().getNamedItem("handler").getNodeValue());

        return this.createNamespace(namespace.getApplicationNamespace());
    }

    @Override
    public LocalNamespace createNamespace(ApplicationNamespace global) {
        return handler == null ? null : new LocalNamespace(handler, global);
    }

    public boolean canHandle(ResourcePool pool, List<String> chatTypes){
        Update update = pool.getUpdate();

        if(!ChatTypes.isApplicable(this.chatTypes == null ? chatTypes : List.of(this.chatTypes), MessageUtils.getChat(update)))
            return false;

        if (this.predicate != null && this.predicate.generate(pool)) {
            return true;
        } else if (this.callbackTriggers != null && update.hasCallbackQuery() && update.getCallbackQuery().getData() != null){
            return Arrays.stream(this.getCallbackTriggers()).anyMatch(c -> c.equals(update.getCallbackQuery().getData()));
        } else if (update.hasMessage() && !MessageUtils.hasMedia(update.getMessage()) && update.getMessage().getText() != null) {
            boolean isCommand = update.getMessage().getText().startsWith(Syntax.COMMAND_START);

            if (isCommand && this.commands != null)
                return Arrays.stream(this.commands)
                        .anyMatch(c -> c.equals(update.getMessage().getText().substring(1)));
            else if (this.keys != null)
                return Arrays.stream(this.keys)
                        .anyMatch(c -> c.equals(update.getMessage().getText()));

            return false;
        }

        return false;
    }

    @Override
    public List<PartialBotApiMethod<?>> getMethods(ResourcePool pool) {
        return actions != null ? this.actions.stream().map(a -> a.generateMethod(pool)).collect(Collectors.toList()) : List.of();
    }
}
