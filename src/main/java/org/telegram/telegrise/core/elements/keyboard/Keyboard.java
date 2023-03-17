package org.telegram.telegrise.core.elements.keyboard;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrise.TelegRiseRuntimeException;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.StorableElement;
import org.telegram.telegrise.core.elements.TranscriptionElement;
import org.telegram.telegrise.core.parser.*;
import org.w3c.dom.Node;

import java.util.List;
import java.util.stream.Collectors;

@Element(name = "keyboard")
@Data @NoArgsConstructor
public class Keyboard implements StorableElement, TranscriptionElement {
    public static final String INLINE = "inline";
    public static final String REPLY = "reply";

    @ElementField(name = "byName")
    private String byName;

    @ElementField(name = "type")
    private String type;

    @ElementField(name = "name")
    private String name;

    @ElementField(name = "create", expression = true)
    private GeneratedValue<ReplyKeyboard> create;

    @InnerElement
    private List<Row> rows;

    @Override
    public void validate(Node node) {
        if (!((type != null && name != null && rows != null) || byName != null || create != null))
            throw new TranscriptionParsingException("Invalid attributes for keyboard", node);
    }

    @Override
    public void load(ParserMemory memory) {
        if (byName != null){
            TranscriptionElement element = memory.get(byName);

            if (element == null)
                throw new TelegRiseRuntimeException("Missing keyboard named '" + byName + "'");
            else if (!(element instanceof Keyboard))
                throw new TelegRiseRuntimeException(String.format("The name '%s' belongs to an object of type '%s', type keyboard is required",
                        byName, element.getClass().getAnnotation(Element.class).name()));

            Keyboard original = (Keyboard) element;
            this.name = original.getName();
            this.type = original.getType();
            this.rows = original.getRows();
            this.byName = null;
        }
    }

    @Override
    public void store(ParserMemory memory) {
        assert name != null : "Unable to store keyboard without name";
        memory.put(name, this);
    }

    public ReplyKeyboard createMarkup(ResourcePool pool){
        if (this.create != null)
            return this.create.generate(pool);

        switch (type.toLowerCase()) {
            case INLINE:
                return new InlineKeyboardMarkup(rows.stream()
                        .filter(r -> r.getWhen().generate(pool))
                        .map(r -> r.createInlineRow(pool)).collect(Collectors.toList()));
            case REPLY:
                return new ReplyKeyboardMarkup(rows.stream()
                        .filter(r -> r.getWhen().generate(pool))
                        .map(r -> r.createKeyboardRow(pool)).collect(Collectors.toList()));
            default:
                throw new TelegRiseRuntimeException("Undefined keyboard type '" + type + "'");
        }
    }

}
