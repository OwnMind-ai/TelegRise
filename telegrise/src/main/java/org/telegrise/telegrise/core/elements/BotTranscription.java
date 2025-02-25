package org.telegrise.telegrise.core.elements;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.TelegramUrl;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScope;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegrise.telegrise.SessionIdentifier;
import org.telegrise.telegrise.TelegRiseApplication;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.base.NodeElement;
import org.telegrise.telegrise.core.elements.head.HeadBlock;
import org.telegrise.telegrise.core.elements.security.Role;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.core.parser.InnerElement;
import org.telegrise.telegrise.core.parser.TranscriptionMemory;
import org.telegrise.telegrise.exceptions.TranscriptionParsingException;
import org.telegrise.telegrise.types.UserRole;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The main element that contains configuration and trees of a Telegram bot.
 * <p>
 * This element must be a root node of an XML file
 * and be specified at {@link TelegRiseApplication#TelegRiseApplication TelegRiseApplication} constructor.
 * <pre>
 * {@code
 * <bot>
 *     <head>
 *     ...
 *     </head>
 *     <root>
 *     ...
 *     </root>
 * </bot>
 * }
 * </pre>
 *
 * @see <a href="https://core.telegram.org/bots/api">Telegram API</a>
 * @since 0.1
 */
@Element(name = "bot")
@Getter @Setter @NoArgsConstructor
public final class BotTranscription extends NodeElement {
    /**
     * Token of this bot to make API calls
     */
    @Attribute(name = "token")
    private GeneratedValue<String> token;

    /**
     * Set to true to allow trees to interrupt each other when their condition is met to be jumped to
     */
    @Attribute(name = "interruptions")
    private boolean interruptions = false;

    /**
     * Set to true to automatically generate a command list based on trees commands and description
     */
    @Attribute(name = "autoCommands")
    private String autoCommands;

    /**
     * Defines a session type of this bot, '{@code chat}' or '{@code user}'
     */
    @Attribute(name = "sessionType")
    private String sessionType;

    /**
     * If set, the application will ignore updates <i>from a specific user</i> that are coming to quickly:
     * in a window of {@code throttlingTime} milliseconds
     */
    @Attribute(name = "throttlingTime")  // ms
    private Integer throttlingTime;

    @InnerElement(priority = 10, nullable = false)
    private HeadBlock head;

    @InnerElement(nullable = false)
    private Root root;

    private TranscriptionMemory memory;
    @Getter
    private Map<String, UserRole> roleMap;

    @Override
    public void load(TranscriptionMemory memory) {
        if (token != null && head.getToken() != null) 
            throw new TranscriptionParsingException("Conflicting configurations: 'token' in <bot> and <token> in <head>", node);

        if (token == null) {
            if (head.getToken() == null)
                throw new TranscriptionParsingException("No bot token was specified. Include 'token' attribute to the <bot> element or add <token>your token</token> in the <head> element", node);

            token = head.getToken().getToken();
        }

        if (sessionType == null)
            sessionType = head.getSessionType() != null ? head.getSessionType().getType() : SessionIdentifier.SESSION_CHAT;

        if(!List.of(SessionIdentifier.SESSION_CHAT, SessionIdentifier.SESSION_USER).contains(sessionType))
            throw new TranscriptionParsingException("Invalid session type, must be 'chat' or 'user'", node);

        if (head.getRoles() != null){
            this.roleMap = new HashMap<>();
            for (Role role : head.getRoles().getRoles())
                roleMap.put(role.getName(), UserRole.ofRole(role));
        }
    }

    @Override
    public void validate(TranscriptionMemory memory) {
        if (head.getRoles() != null)
            head.getRoles().getRoles().stream()
                    .peek(r -> {
                        if (r.getOnDeniedTree() != null && (!memory.containsKey(r.getOnDeniedTree()) || !(memory.get(r.getOnDeniedTree()) instanceof Tree)))
                            throw new TranscriptionParsingException("Role '" + r.getName() + "' refers to a non-existent tree '" + r.getOnDeniedTree() + "' in attribute 'onDeniedTree'", r.getElementNode());
                    })
                    .filter(r -> r.getTrees() != null)
                    .map(r -> Pair.of(r, r.getTrees()))
                    .flatMap(p -> Arrays.stream(p.getRight()).map(t -> Pair.of(t, p.getLeft())))
                    .forEach(pair -> {
                        String tree = pair.getLeft();
                        Role role = pair.getRight();
                        if (!memory.containsKey(tree) || !(memory.get(tree) instanceof Tree))
                            throw new TranscriptionParsingException("Role '" + role.getName() + "' gives access to a non-existent tree '" + tree + "'", role.getElementNode());
                    });
    }

    public SetMyCommands getSetCommands(BotCommandScope scope){
        return new SetMyCommands(this.root.getTrees().stream()
                .filter(t -> t.isProducesBotCommands(scope, this.root))
                .map(Tree::getBotCommands)
                .flatMap(List::stream).collect(Collectors.toList()), scope, null);  //TODO add language support
    }

    public void setRoot(Root root) {
        this.root = root;
        root.setTrees(new ArrayList<>(root.getTrees() == null ? List.of() :root.getTrees()));
    }

    public boolean isWebhookBot() {
        return head != null && head.getWebhook() != null && head.getWebhook().getEnabled().generate(new ResourcePool());
    }

    public TelegramUrl getTelegramUrl(){
        ResourcePool pool = new ResourcePool();
        return head.getTelegramUrl() == null || !head.getTelegramUrl().getEnabled().generate(pool) ? TelegramUrl.DEFAULT_URL
                : head.getTelegramUrl().produceTelegramUrl(pool);
    }

    public TelegramClient produceClient() {
        return new OkHttpTelegramClient(token.generate(new ResourcePool()), this.getTelegramUrl());
    }
}