package org.telegram.telegrise.core.elements;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScope;
import org.telegram.telegrise.core.elements.head.HeadBlock;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.InnerElement;

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

    @Attribute(name = "defaultParseMode")
    private String defaultParseMode = "html";

    @Attribute(name = "autoCommands")
    private String autoCommands;

    @InnerElement(nullable = false)
    private Menu rootMenu;

    @InnerElement(priority = 10)
    private HeadBlock head;

    public SetMyCommands getSetCommands(BotCommandScope scope){
        return new SetMyCommands(this.rootMenu.getTrees().stream()
                .filter(t -> t.isProducesBotCommands(scope, this.rootMenu))
                .map(Tree::getBotCommands)
                .flatMap(List::stream).collect(Collectors.toList()), scope, null);  //FIXME add language support
    }
}
