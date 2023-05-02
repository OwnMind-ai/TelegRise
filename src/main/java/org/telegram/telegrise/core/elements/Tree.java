package org.telegram.telegrise.core.elements;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScope;
import org.telegram.telegrise.ChatTypes;
import org.telegram.telegrise.MessageUtils;
import org.telegram.telegrise.core.ApplicationNamespace;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.LocalNamespace;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.actions.ActionElement;
import org.telegram.telegrise.core.parser.*;
import org.telegram.telegrise.types.CommandData;
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Element(name = "tree")
@Data
@NoArgsConstructor
public class Tree implements BranchingElement{
    @Attribute(name = "name", nullable = false)
    private String name;
    @Attribute(name = "interruptible")
    private boolean interruptible = true;

    @Attribute(name = "commands")
    private String[] commands;
    @Attribute(name = "keys")
    private String[] keys;
    @Attribute(name = "callbackTriggers")
    private String[] callbackTriggers;
    @Attribute(name = "predicate")
    private GeneratedValue<Boolean> predicate;
    @Attribute(name = "chats")
    private String[] chatTypes;

    @Attribute(name = "description")
    private String description;

    @Attribute(name = "commandScopes")
    private String[] scopes;

    @Attribute(name = "accessLevel")
    private Integer accessLevel;

    private Class<?> controller;

    @InnerElement
    private List<ActionElement> actions;
    @InnerElement
    private List<Branch> branches;
    @InnerElement
    private List<Menu> menus;
    @InnerElement
    private DefaultBranch defaultBranch;

    @Override
    public void validate(Node node, TranscriptionMemory memory) {
        if(this.controller != null && (this.branches == null || this.branches.isEmpty()))
            throw new TranscriptionParsingException("Trees with no branches cannot be connected to a controller", node);
    }

    @Attribute(priority = Double.POSITIVE_INFINITY)
    private LocalNamespace extractHandler(Node node, LocalNamespace namespace){
        if (node.getAttributes().getNamedItem("controller") != null)
            this.controller = namespace.getApplicationNamespace().getClass(node.getAttributes().getNamedItem("controller").getNodeValue());

        return this.createNamespace(namespace.getApplicationNamespace());
    }

    @Override
    public LocalNamespace createNamespace(ApplicationNamespace global) {
        return new LocalNamespace(controller, global);
    }

    public boolean canHandleMessage(ResourcePool pool){
        Update update = pool.getUpdate();

        if (!update.hasMessage() || update.getMessage().getText() == null) return false;

        if (this.commands != null) {
            CommandData command = MessageUtils.parseCommand(update.getMessage().getText());
            boolean isUserChat = Objects.requireNonNull(MessageUtils.getChat(update)).isUserChat();

            if (command != null && (isUserChat || pool.getMemory().getBotUsername().equals(command.getUsername())))
                return Arrays.stream(this.commands)
                        .anyMatch(c -> c.equals(command.getName()));
        }

        if (this.keys != null)
            return Arrays.stream(this.keys)
                    .anyMatch(c -> c.equals(update.getMessage().getText()));

        return false;
    }

    public boolean isProducesBotCommands(BotCommandScope scope, Menu rootMenu){
        List<String> scopes = this.scopes != null ? List.of(this.scopes) :
                ChatTypes.chatTypesToScopes(Objects.requireNonNullElse(this.chatTypes ,rootMenu.getChatTypes()));

        return this.description != null && ChatTypes.isApplicable(scopes, scope);
    }

    public List<BotCommand> getBotCommands(){
        assert description != null;
        return Arrays.stream(this.commands).map(c -> new BotCommand(c, description)).collect(Collectors.toList());
    }

    public boolean isChatApplicable(List<String> chatTypes, Chat chat) {
        return ChatTypes.isApplicable(this.chatTypes == null ? chatTypes : List.of(this.chatTypes), chat);
    }
}
