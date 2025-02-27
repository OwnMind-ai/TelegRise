package org.telegrise.telegrise.core.elements;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScope;
import org.telegrise.telegrise.Expression;
import org.telegrise.telegrise.annotations.OnClose;
import org.telegrise.telegrise.annotations.OnCreate;
import org.telegrise.telegrise.annotations.TreeController;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.actions.ActionElement;
import org.telegrise.telegrise.core.elements.base.BranchingElement;
import org.telegrise.telegrise.core.elements.base.NodeElement;
import org.telegrise.telegrise.core.elements.keyboard.Keyboards;
import org.telegrise.telegrise.core.elements.text.Texts;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.*;
import org.telegrise.telegrise.exceptions.TranscriptionParsingException;
import org.telegrise.telegrise.types.CommandData;
import org.telegrise.telegrise.utils.ChatTypes;
import org.telegrise.telegrise.utils.MessageUtils;
import org.w3c.dom.Node;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A main unit in a branching system of bot's transcription.
 * This element must be placed inside a {@code <root>} or {@code <trees>}.
 *
 * @since 0.1
 */
@Element(name = "tree")
@Getter @Setter @NoArgsConstructor
public class Tree extends NodeElement implements org.telegrise.telegrise.transcription.Tree, BranchingElement {
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

    /**
     * Name of the tree
     */
    @Attribute(name = "name", nullable = false)
    private String name;
    /**
     * Defines types of interruptions that allowed in this tree
     */
    @Attribute(name = "allowedInterruptions")
    private String[] allowedInterruptions = {INTERRUPT_BY_ALL};

    /**
     * Command or list of command that this tree will respond to
     */
    @Attribute(name = "command")
    private String[] commands;
    /**
     * Text of a message (the key) or list of them that this tree will respond to
     */
    @Attribute(name = "key")
    private String[] keys;
    /**
     * Callback data or a list of them that this tree will respond to
     */
    @Attribute(name = "callback")
    private String[] callbackTriggers;
    /**
     * An expression that determines if this tree should respond to an update.
     * This attribute <b>cannot</b> use method references from attached controller
     */
    @Attribute(name = "predicate")
    private GeneratedValue<Boolean> predicate;
    /**
     * Types of chat that this tree can be used in
     */
    @Attribute(name = "chats")
    private String[] chatTypes;

    /**
     * Description to this tree that can be used to generate a command list
     */
    @Attribute(name = "description")
    private String description;

    /**
     * Chat scopes that command of this tree belongs to
     * @see <a href="https://core.telegram.org/bots/api#botcommandscope">Telegram API: BotCommandScope</a>
     */
    @Attribute(name = "commandScopes")
    private String[] scopes;

    /**
     * Users of lesser access level than specified will not be able to use this tree.
     */
    @Attribute(name = "accessLevel")
    private Integer accessLevel;

    private Class<?> controller;

    @InnerElement(priority = 100)
    private Keyboards keyboards;
    @InnerElement(priority = 100)
    private Texts texts;
    @InnerElement
    private List<ActionElement> actions;
    @InnerElement
    private List<Branch> branches;
    @InnerElement
    private DefaultBranch defaultBranch;

    private int level = -1;

    @Override
    public void validate(TranscriptionMemory memory) {
        if (improperInterruptionScopes(this.allowedInterruptions))
            throw new TranscriptionParsingException("Undefined interruption scopes", node);

        if (this.controller != null)
            this.validateSuperclass(this.controller, this.controller.getSuperclass());
    }

    /**
     * Controller for this tree that provides backend logic to the tree such as method references.
     */
    @Attribute(name = "controller", priority = Double.POSITIVE_INFINITY)
    private LocalNamespace extractController(Node node, LocalNamespace namespace){
        if (node.getAttributes().getNamedItem("controller") != null) {
            this.controller = namespace.getApplicationNamespace().getClass(node.getAttributes().getNamedItem("controller").getNodeValue());
            if (!controller.isAnnotationPresent(TreeController.class))
                throw new TranscriptionParsingException("Tree uses a controller the class of which is not annotated with @TreeController", node);
        }

        return new LocalNamespace(this.controller, namespace.getApplicationNamespace());
    }

    private void validateSuperclass(Class<?> child, Class<?> superclass) {
        if (superclass == null || superclass.equals(Object.class)) return;

        for (Method m : superclass.getDeclaredMethods())
            if (m.isAnnotationPresent(OnCreate.class) || m.isAnnotationPresent(OnClose.class))
                throw new TranscriptionParsingException("Unable to initialize class '%s': superclass '%s' has method '%s' that is annotated with @OnCreate or @OnClose"
                        .formatted(child.getSimpleName(), superclass.getSimpleName(), m.getName()), node);

        validateSuperclass(child, superclass.getSuperclass());
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

        //TODO Add an option to allow/restrict anon commands in groups (disable 'command.username() == null' bellow)
        if (command != null && (isUserChat || command.username() == null || pool.getBotUser().getUsername().equals(command.username())))
            return Arrays.stream(this.commands)
                    .anyMatch(c -> c.equals(command.name()));

        return false;
    }

    public boolean isProducesBotCommands(BotCommandScope scope, Root rootMenu){
        List<String> scopes = this.scopes != null ? List.of(this.scopes) :
                ChatTypes.chatTypesToScopes(
                        Optional.ofNullable(this.chatTypes)
                                .or(() -> Optional.ofNullable(rootMenu.getChatTypes()))
                                .orElse(new String[]{ChatTypes.DEFAULT_SCOPE}));

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
        return branches;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tree tree = (Tree) o;
        return name.equals(tree.name);
    }

    @Override
    public void store(TranscriptionMemory memory) {
        memory.put(null, this.getName(), this);
    }

    @Override
    public String toString(){
        return "Tree(" + name + ")";
    }

    @Override
    public @Nullable Expression<Boolean> getPredicateExpression() {
        return predicate != null ? predicate.toExpression() : null;
    }
}
