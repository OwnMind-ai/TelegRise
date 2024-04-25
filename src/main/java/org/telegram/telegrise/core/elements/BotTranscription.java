package org.telegram.telegrise.core.elements;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScope;
import org.telegram.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegram.telegrise.core.elements.head.HeadBlock;
import org.telegram.telegrise.core.elements.security.Role;
import org.telegram.telegrise.core.elements.security.Roles;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.InnerElement;
import org.telegram.telegrise.core.parser.TranscriptionMemory;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Element(name = "bot")
@Data
@NoArgsConstructor
public final class BotTranscription implements TranscriptionElement {

    //TODO webhooks support
    @Attribute(name = "username", nullable = false)
    private String username;

    @Attribute(name = "token", nullable = false)
    private String token;

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
    private Menu rootMenu;

    @InnerElement(priority = -1)
    private Roles roles;

    @EqualsAndHashCode.Exclude
    private TranscriptionMemory memory;

    @Override
    public void validate(Node node, TranscriptionMemory memory) {
        if (roles != null)
            roles.getRoles().stream()
                    .peek(r -> {
                        if (r.getOnDeniedTree() != null && (!memory.containsKey(r.getOnDeniedTree()) || !(memory.get(r.getOnDeniedTree()) instanceof Tree)))
                            throw new TelegRiseRuntimeException("Role '" + r.getName() + "' refers to a non-existent tree '" + r.getOnDeniedTree() + "' at attribute 'onDeniedTree'");
                    })
                    .filter(r -> r.getTrees() != null)
                    .map(r -> Pair.of(r, r.getTrees()))
                    .flatMap(p -> Arrays.stream(p.getRight()).map(t -> Pair.of(t, p.getLeft())))
                    .forEach(pair -> {
                        String tree = pair.getLeft();
                        Role role = pair.getRight();
                        if (!memory.containsKey(tree) || !(memory.get(tree) instanceof Tree))
                            throw new TelegRiseRuntimeException("Role '" + role.getName() + "' gives access to a non-existent tree '" + tree + "'");
                    });
    }

    public SetMyCommands getSetCommands(BotCommandScope scope){
        return new SetMyCommands(this.rootMenu.getTrees().stream()
                .filter(t -> t.isProducesBotCommands(scope, this.rootMenu))
                .map(Tree::getBotCommands)
                .flatMap(List::stream).collect(Collectors.toList()), scope, null);  //FIXME add language support
    }

    public void setRootMenu(Menu rootMenu) {
        this.rootMenu = rootMenu;
        rootMenu.setTrees(new ArrayList<>(rootMenu.getTrees()));
    }
}
