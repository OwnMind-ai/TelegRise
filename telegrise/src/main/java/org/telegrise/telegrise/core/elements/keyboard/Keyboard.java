package org.telegrise.telegrise.core.elements.keyboard;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegrise.telegrise.Expression;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.SessionMemoryImpl;
import org.telegrise.telegrise.core.elements.Tree;
import org.telegrise.telegrise.core.elements.base.InteractiveElement;
import org.telegrise.telegrise.core.elements.base.NamedElement;
import org.telegrise.telegrise.core.elements.base.NodeElement;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.core.parser.InnerElement;
import org.telegrise.telegrise.core.parser.TranscriptionMemory;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegrise.telegrise.exceptions.TranscriptionParsingException;
import org.telegrise.telegrise.keyboard.KeyboardMarkup;
import org.telegrise.telegrise.keyboard.KeyboardState;
import org.telegrise.telegrise.types.UserRole;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Element(name = "keyboard", finishAfterParsing = true)
@Getter @Setter @NoArgsConstructor
public class Keyboard extends NodeElement implements InteractiveElement<KeyboardMarkup>, NamedElement, org.telegrise.telegrise.transcription.Keyboard {
    public static final String INLINE = "inline";
    public static final String REPLY = "reply";

    public static boolean filterKeyboardElement(GeneratedValue<Boolean> when, Integer accessLevel, ResourcePool pool){
        if (accessLevel != null) {
            UserRole userRole = pool.getMemory().getUserRole();
            return userRole != null && userRole.level() >= accessLevel;
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

    @Attribute(name = "global")
    private boolean global;

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
        if (!((type != null && rows != null) || byName != null || create != null))
            throw new TranscriptionParsingException("Invalid attributes for keyboard", node);

        if (type != null && !List.of(REPLY, INLINE).contains(type))
            throw new TranscriptionParsingException("Invalid keyboard type '" + type + "'", node);

        if(create != null && rows != null && !rows.isEmpty())
            throw new TranscriptionParsingException("Keyboards with 'create' attribute cannot have children elements", node);
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
            this.name = Objects.requireNonNullElse(this.name, original.getName());
            this.type = original.getType();
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
        if (name != null)    // Method ::store runs before ::load
            memory.put(parentTree, name, this);
    }

    public ReplyKeyboard createMarkup(ResourcePool pool){
        if (this.create != null)
            return this.create.generate(pool);

        SessionMemoryImpl memory = pool.getMemory();
        KeyboardState state = name != null ? memory.getKeyboardState(name, parentTree) : null;
        if(name != null && state == null){
            state = new KeyboardState(Objects.requireNonNullElse(memory.getCurrentBranch(), parentTree), this, pool);
            memory.putKeyboardState(name, parentTree, state);
        }

        switch (type.toLowerCase()) {
            case INLINE:
                List<InlineKeyboardRow> inlineRows = new ArrayList<>();

                for (int i = 0; i < rows.size(); i++) {
                    Row r = rows.get(i);
                    if (filterKeyboardElement(r.getWhen(), r.getAccessLevel(), pool)) {
                        var inlineRow = r.createInlineRow(pool, state, i);
                        if (!inlineRow.isEmpty()) 
                            inlineRows.add(inlineRow);
                    }
                }

                return new InlineKeyboardMarkup(inlineRows);
            case REPLY:
                List<KeyboardRow> replyRows = new ArrayList<>();

                for (int i = 0; i < rows.size(); i++) {
                    Row r = rows.get(i);
                    if (filterKeyboardElement(r.getWhen(), r.getAccessLevel(), pool)) {
                        var row = r.createKeyboardRow(pool, state, i);
                        if (!row.isEmpty()) 
                            replyRows.add(row);
                    }
                }

                ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(replyRows);

                keyboard.setIsPersistent(generateNullableProperty(persistent, pool));
                keyboard.setResizeKeyboard(generateNullableProperty(resize, pool));
                keyboard.setOneTimeKeyboard(generateNullableProperty(oneTime, pool));
                keyboard.setInputFieldPlaceholder(generateNullableProperty(placeholder, pool));
                keyboard.setSelective(generateNullableProperty(selective, pool));

                return keyboard;
            default:
                throw new TelegRiseRuntimeException("Undefined keyboard type '" + type + "'", node);
        }
    }

    @Override
    public KeyboardMarkup createInteractiveObject(Function<Update, ResourcePool> resourcePoolFunction) {
        return new KeyboardMarkup(this, resourcePoolFunction);
    }

    @Override
    public Expression<ReplyKeyboard> getMarkupExpression() {
        return new Expression<>(this::createMarkup);
    }
}
