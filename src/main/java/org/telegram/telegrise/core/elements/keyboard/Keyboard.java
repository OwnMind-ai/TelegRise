package org.telegram.telegrise.core.elements.keyboard;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrise.SessionMemory;
import org.telegram.telegrise.TelegRiseRuntimeException;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.StorableElement;
import org.telegram.telegrise.core.elements.TranscriptionElement;
import org.telegram.telegrise.core.parser.*;
import org.telegram.telegrise.types.DynamicKeyboard;
import org.telegram.telegrise.types.UserRole;
import org.w3c.dom.Node;

import java.util.List;
import java.util.stream.Collectors;

@Element(name = "keyboard")
@Data @NoArgsConstructor
public class Keyboard implements StorableElement, TranscriptionElement {
    public static final String INLINE = "inline";
    public static final String REPLY = "reply";

    public static boolean filterKeyboardElement(GeneratedValue<Boolean> when, Integer accessLevel, ResourcePool pool){
        UserRole userRole = pool.getMemory().getUserRole();

        if (accessLevel != null)
            return userRole != null && userRole.getLevel() >= accessLevel;
        if (when != null)
            return when.generate(pool);

        return true;
    }

    @Attribute(name = "byName")
    private String byName;

    @Attribute(name = "type")
    private String type;

    @Attribute(name = "name")
    private String name;

    @Attribute(name = "dynamic")
    private boolean dynamic;

    @Attribute(name = "id")
    private String id;

    @Attribute(name = "create")
    private GeneratedValue<ReplyKeyboard> create;

    @Attribute(name = "isPersistent")
    private GeneratedValue<Boolean> isPersistent;

    @Attribute(name = "oneTime")
    private GeneratedValue<Boolean> oneTime;

    @Attribute(name = "resize")
    private GeneratedValue<Boolean> resize;

    @Attribute(name = "selective")
    private GeneratedValue<Boolean> selective;

    @Attribute(name = "placeholder")
    private GeneratedValue<String> placeholder;

    @InnerElement
    private List<Row> rows;

    @Override
    public void validate(Node node, TranscriptionMemory memory) {
        if (!((type != null && rows != null) || byName != null || create != null))
            throw new TranscriptionParsingException("Invalid attributes for keyboard", node);

        if (dynamic && (id == null || id.length() < 1))
            throw new TranscriptionParsingException("Dynamic keyboard must have an id attribute", node);

        if (dynamic && create != null)
            throw new TranscriptionParsingException("'create' attribute conflicts with dynamic keyboard declaration", node);
    }

    @Override
    public void load(TranscriptionMemory memory) {
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
        }
    }

    @Override
    public void store(TranscriptionMemory memory) {
         if (name != null && byName == null)
            memory.put(name, this);
    }

    public ReplyKeyboard createMarkup(ResourcePool pool){
        if (this.dynamic)
            return extractDynamic(pool);

        if (this.create != null)
            return this.create.generate(pool);

        switch (type.toLowerCase()) {
            case INLINE:
                return new InlineKeyboardMarkup(rows.stream()
                        .filter(r -> filterKeyboardElement(r.getWhen(), r.getAccessLevel(), pool))
                        .map(r -> r.createInlineRow(pool)).collect(Collectors.toList()));
            case REPLY:
                ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(rows.stream()
                        .filter(r -> filterKeyboardElement(r.getWhen(), r.getAccessLevel(), pool))
                        .map(r -> r.createKeyboardRow(pool)).collect(Collectors.toList()));

                keyboard.setIsPersistent(generateNullableProperty(isPersistent, pool));
                keyboard.setResizeKeyboard(generateNullableProperty(resize, pool));
                keyboard.setOneTimeKeyboard(generateNullableProperty(oneTime, pool));
                keyboard.setInputFieldPlaceholder(generateNullableProperty(placeholder, pool));
                keyboard.setSelective(generateNullableProperty(selective, pool));

                return keyboard;
            default:
                throw new TelegRiseRuntimeException("Undefined keyboard type '" + type + "'");
        }
    }

    public ReplyKeyboard extractDynamic(ResourcePool pool){
        assert pool.getMemory() != null : "Unable to create dynamic keyboard: session memory is null";
        SessionMemory memory = pool.getMemory();

        if (memory.containsKey(this.id)){
            DynamicKeyboard keyboard = memory.get(this.id, DynamicKeyboard.class);

            switch (type.toLowerCase()) {
                case INLINE:
                    return keyboard.createInline(pool);
                case REPLY:
                    return keyboard.createReply(pool);
                default:
                    throw new TelegRiseRuntimeException("Undefined keyboard type '" + type + "'");
            }
        } else {
            DynamicKeyboard keyboard = DynamicKeyboard.ofKeyboard(this);
            memory.put(this.id, keyboard);

            return this.extractDynamic(pool);
        }
    }
}
