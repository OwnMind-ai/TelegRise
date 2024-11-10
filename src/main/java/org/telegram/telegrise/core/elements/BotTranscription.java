package org.telegram.telegrise.core.elements;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScope;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.elements.head.HeadBlock;
import org.telegram.telegrise.core.elements.security.Role;
import org.telegram.telegrise.core.elements.security.Roles;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.InnerElement;
import org.telegram.telegrise.core.parser.TranscriptionMemory;
import org.telegram.telegrise.exceptions.TranscriptionParsingException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Element(name = "bot")
@Getter @Setter
@NoArgsConstructor
public final class BotTranscription extends NodeElement {

    //TODO webhooks support
    @Attribute(name = "username")
    private GeneratedValue<String> username;

    @Attribute(name = "token")
    private GeneratedValue<String> token;

    @Attribute(name = "interruptions")
    private boolean interruptions = false;

    @Attribute(name = "defaultParseMode")
    private String defaultParseMode = "html";

    @Attribute(name = "autoCommands")
    private String autoCommands;

    @Attribute(name = "throttlingTime")  // ms
    private Integer throttlingTime;

    @InnerElement(priority = 10)
    private HeadBlock head;

    @InnerElement(nullable = false)
    private Root root;

    @InnerElement(priority = -1)
    private Roles roles;

    private TranscriptionMemory memory;

    @Override
    public void load(TranscriptionMemory memory) {
        if (token != null && head.getToken() != null) 
            throw new TranscriptionParsingException("Conflicting configurations: 'token' in <bot> and <token> in <head>", node);

        if (username != null && head.getUsername() != null) 
            throw new TranscriptionParsingException("Conflicting configurations: 'username' in <bot> and <username> in <head>", node);

        if (token == null) {
            if (head.getToken() == null)
                throw new TranscriptionParsingException("No bot token was specified. Include 'token' attribute to the <bot> element or add <token>your token</token> in the <head> element", node);

            token = head.getToken().getToken();
        }

        if (username == null && head.getUsername() != null) 
            username = head.getUsername().getUsername();
    }

    @Override
    public void validate(TranscriptionMemory memory) {
        if (roles != null)
            roles.getRoles().stream()
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
                .flatMap(List::stream).collect(Collectors.toList()), scope, null);  //FIXME add language support
    }

    public void setRoot(Root root) {
        this.root = root;
        root.setTrees(new ArrayList<>(root.getTrees() == null ? List.of() :root.getTrees()));
    }
}