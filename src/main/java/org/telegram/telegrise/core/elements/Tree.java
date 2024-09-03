package org.telegram.telegrise.core.elements;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScope;
import org.telegram.telegrise.ChatTypes;
import org.telegram.telegrise.core.ApplicationNamespace;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.LocalNamespace;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.actions.ActionElement;
import org.telegram.telegrise.core.elements.keyboard.Keyboards;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.InnerElement;
import org.telegram.telegrise.core.parser.TranscriptionMemory;
import org.telegram.telegrise.exceptions.TranscriptionParsingException;
import org.telegram.telegrise.types.CommandData;
import org.telegram.telegrise.utils.MessageUtils;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Element(name = "tree")
@Data
@NoArgsConstructor
public class Tree extends NodeElement implements BranchingElement{
    public static final String INTERRUPT_BY_CALLBACKS = "callbacks";
    public static final String INTERRUPT_BY_KEYS = "keys";
    public static final String INTERRUPT_BY_COMMANDS = "commands";
    public static final String INTERRUPT_BY_PREDICATES = "predicates";
    public static final String INTERRUPT_BY_ALL = "all";
    public static final String INTERRUPT_BY_NONE = "none";

    public static boolean improperInterruptionScopes(String[] scopes){
        List<String> scopesList = List.of(scopes);
        if (scopesList.contains(INTERRUPT_BY_NONE))
            return scopes.length != 1;

        return !scopesList.stream().allMatch(s -> s.equals(INTERRUPT_BY_CALLBACKS) || s.equals(INTERRUPT_BY_KEYS)
                || s.equals(INTERRUPT_BY_COMMANDS) || s.equals(INTERRUPT_BY_PREDICATES) || s.equals(INTERRUPT_BY_ALL));
    }

    @Attribute(name = "name", nullable = false)
    private String name;
    @Attribute(name = "allowedInterruptions")
    private String[] allowedInterruptions = {INTERRUPT_BY_ALL};

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

    @InnerElement(priority = 100)
    private Keyboards keyboards;
    @InnerElement
    private List<ActionElement> actions;
    @InnerElement
    private List<Branch> branches;
    @InnerElement
    private List<Menu> menus;
    @InnerElement
    private DefaultBranch defaultBranch;

    private int level = -1;

    @Override
    public void validate(TranscriptionMemory memory) {
        if(this.controller != null && (this.branches == null || this.branches.isEmpty()))
            throw new TranscriptionParsingException("Trees with no branches cannot be connected to a controller", node);

        if (improperInterruptionScopes(this.allowedInterruptions))
            throw new TranscriptionParsingException("Undefined interruption scopes", node);
    }

    @Attribute(name = "controller", priority = Double.POSITIVE_INFINITY)
    private LocalNamespace extractHandler(Node node, LocalNamespace namespace){
        if (node.getAttributes().getNamedItem("controller") != null)
            this.controller = namespace.getApplicationNamespace().getClass(node.getAttributes().getNamedItem("controller").getNodeValue());

        return this.createNamespace(namespace.getApplicationNamespace());
    }

    @Override
    public LocalNamespace createNamespace(ApplicationNamespace global) {
        return new LocalNamespace(controller, global);
    }

    public boolean canHandleMessage(ResourcePool pool, Chat chat){
        Update update = pool.getUpdate();

        if (update.hasCallbackQuery() && this.callbackTriggers != null){
            for(String trigger : callbackTriggers){
                if(trigger.equals(update.getCallbackQuery().getData())){
                    return true;
                }
            }

            return false;
        }

        if (!update.hasMessage() || update.getMessage().getText() == null) return false;

        if (this.commands != null) {
            if (isApplicableCommand(update.getMessage().getText(), chat, pool)){
                return true;
            }
        }

        if (this.keys != null)
            return Arrays.stream(this.keys)
                    .anyMatch(c -> c.equals(update.getMessage().getText()));

        return false;
    }

    public boolean isApplicableCommand(String text, Chat chat, ResourcePool pool){
        CommandData command = MessageUtils.parseCommand(text);
        boolean isUserChat = Objects.requireNonNull(chat).isUserChat();

        if (command != null && (isUserChat || pool.getMemory().getBotUsername().equals(command.getUsername())))
            return Arrays.stream(this.commands)
                    .anyMatch(c -> c.equals(command.getName()));

        return false;
    }

    public boolean isProducesBotCommands(BotCommandScope scope, Menu rootMenu){
        List<String> scopes = this.scopes != null ? List.of(this.scopes) :
                ChatTypes.chatTypesToScopes(Objects.requireNonNullElse(this.chatTypes ,rootMenu.getChatTypes()));

        return this.description != null && this.commands != null && ChatTypes.isApplicable(scopes, scope);
    }

    public List<BotCommand> getBotCommands(){
        assert description != null;
        return Arrays.stream(this.commands).map(c -> new BotCommand(c, description)).collect(Collectors.toList());
    }

    public boolean isChatApplicable(List<String> chatTypes, Chat chat) {
        return ChatTypes.isApplicable(this.chatTypes == null ? chatTypes : List.of(this.chatTypes), chat);
    }

    @Override
    public List<? extends BranchingElement> getChildren() {
        var result = new ArrayList<BranchingElement>();
        if (branches != null) result.addAll(branches);
        if (menus != null) result.addAll(menus);

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tree tree = (Tree) o;
        return name.equals(tree.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public void store(TranscriptionMemory memory) {
        memory.put(null, this.getName(), this);
    }
}
