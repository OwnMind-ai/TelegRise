package org.telegram.telegrise.core.elements.keyboard;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrise.SessionMemory;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.NamedElement;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.InteractiveElement;
import org.telegram.telegrise.core.elements.NodeElement;
import org.telegram.telegrise.core.elements.Tree;
import org.telegram.telegrise.core.parser.Attribute;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.InnerElement;
import org.telegram.telegrise.core.parser.TranscriptionMemory;
import org.telegram.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegram.telegrise.exceptions.TranscriptionParsingException;
import org.telegram.telegrise.keyboard.DynamicKeyboard;
import org.telegram.telegrise.types.KeyboardMarkup;
import org.telegram.telegrise.types.UserRole;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = false)
@Element(name = "keyboard", validateAfterParsing = true)
@Data @NoArgsConstructor
public class Keyboard extends NodeElement implements InteractiveElement<KeyboardMarkup>, NamedElement {
    public static final String INLINE = "inline";
    public static final String REPLY = "reply";

    public static boolean filterKeyboardElement(GeneratedValue<Boolean> when, Integer accessLevel, ResourcePool pool){
        if (accessLevel != null) {
            UserRole userRole = pool.getMemory().getUserRole();
            return userRole != null && userRole.getLevel() >= accessLevel;
        }

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

    @Attribute(name = "autoClosable")
    private boolean autoClosable = true;

    @Attribute(name = "global")
    private boolean global;

    @Attribute(name = "filler")
    private GeneratedValue<Void> filler;

    @Attribute(name = "id")
    private String id;

    @Attribute(name = "create")
    private GeneratedValue<ReplyKeyboard> create;

    @Attribute(name = "isPersistent")
    private GeneratedValue<Boolean> persistent;

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

    private Tree parentTree;

    @Override
    public void validate(TranscriptionMemory memory) {
        if (!((type != null && rows != null) || byName != null || create != null || (dynamic && filler != null)))
            throw new TranscriptionParsingException("Invalid attributes for keyboard", node);

        if (dynamic && (id == null || id.isEmpty()))
            throw new TranscriptionParsingException("Dynamic keyboard must have an id attribute", node);

        if (dynamic && create != null)
            throw new TranscriptionParsingException("'create' attribute conflicts with dynamic keyboard declaration, use 'filler' attribute to modify keyboard before action execution", node);
    }

    @Override
    public void load(TranscriptionMemory memory) {
        if (byName != null){
            NodeElement element = memory.get(parentTree, byName);

            if (element == null)
                throw new TranscriptionParsingException("Missing keyboard named '" + byName + "'", node);
            else if (!(element instanceof Keyboard))
                throw new TranscriptionParsingException(String.format("The name '%s' belongs to an object of type '%s', type keyboard is required",
                        byName, element.getClass().getAnnotation(Element.class).name()), node);

            Keyboard original = (Keyboard) element;
            this.name = original.getName();
            this.type = original.getType();
            this.dynamic = original.isDynamic();
            this.id = original.getId();
            this.create = original.getCreate();
            this.persistent = original.getPersistent();
            this.oneTime = original.getOneTime();
            this.resize = original.getResize();
            this.selective = original.getSelective();
            this.placeholder = original.getPlaceholder();
            this.rows = original.getRows();
        }
    }

    @Override
    public void store(TranscriptionMemory memory) {
         if (name != null && byName == null)
            memory.put(parentTree, name, this);
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
                        .map(r -> r.createInlineRow(pool))
                        .filter(r -> !r.isEmpty())
                        .collect(Collectors.toList()));
            case REPLY:
                ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(rows.stream()
                        .filter(r -> filterKeyboardElement(r.getWhen(), r.getAccessLevel(), pool))
                        .map(r -> r.createKeyboardRow(pool))
                        .filter(r -> !r.isEmpty())
                        .collect(Collectors.toList()));

                keyboard.setIsPersistent(generateNullableProperty(persistent, pool));
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

            return switch (type.toLowerCase()) {
                case INLINE -> keyboard.createInline(pool);
                case REPLY -> keyboard.createReply(pool);
                default -> throw new TelegRiseRuntimeException("Undefined keyboard type '" + type + "'");
            };
        } else {
            DynamicKeyboard keyboard = DynamicKeyboard.ofKeyboard(this, pool);
            memory.put(this.id, keyboard);

            if (filler != null) filler.generate(pool);
            keyboard.reloadSwitches();

            if (pool.getCurrentExecutor() != null && autoClosable)
                pool.getCurrentExecutor().connectKeyboard(this.id);

            return this.extractDynamic(pool);
        }
    }

    @Override
    public KeyboardMarkup createInteractiveObject(Function<Update, ResourcePool> resourcePoolFunction) {
        return new KeyboardMarkup(this, resourcePoolFunction);
    }
}
