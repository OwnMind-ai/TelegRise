package org.telegram.telegrise.keyboard;

import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.keyboard.Keyboard;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a dynamic keyboard object.
 * DynamicKeyboard contains list of DynamicRows and attributes needed for ReplyKeyboardMarkup.
 * <p>
 * Dynamic keyboard can be declared in transcription using following code (replace square brackets with corresponding values): 
 * <pre>
 * &lt;keyboard type="[inline/reply] dynamic="true" id="[YourKeyboardID]"&gt;...&lt;/keyboard&gt;
 * </pre>
 * Once a dynamic keyboard has been created, it will be placed in <code>SessionMemory</code> with the ID provided as a key v. * 
 * 
 * @see DynamicButton
 * @see DynamicRow
 * @since 0.4
 */
public final class DynamicKeyboard implements Serializable {
    public static DynamicKeyboard ofKeyboard(Keyboard keyboard, ResourcePool pool){
        DynamicKeyboard dynamicKeyboard = new DynamicKeyboard();

        if (keyboard.getRows() != null) {
            dynamicKeyboard.getRows().addAll(keyboard.getRows().stream().map(row -> {
                DynamicRow dynamicRow = DynamicRow.ofRow(row, pool);
                dynamicRow.getButtons().stream().filter(SwitchButton.class::isInstance)
                        .forEach(s -> dynamicKeyboard.switches.put(((SwitchButton) s).getName(), (SwitchButton) s));

                return dynamicRow;
            }).collect(Collectors.toList()));
        }

        dynamicKeyboard.isPersistent = keyboard.getIsPersistent();
        dynamicKeyboard.oneTime = keyboard.getOneTime();
        dynamicKeyboard.resize = keyboard.getResize();
        dynamicKeyboard.selective = keyboard.getSelective();
        dynamicKeyboard.placeholder = keyboard.getPlaceholder();

        return dynamicKeyboard;
    }

    @Getter
    private final List<DynamicRow> rows;
    private final Map<String, SwitchButton> switches = new HashMap<>();
    private GeneratedValue<Boolean> isPersistent;
    private GeneratedValue<Boolean> oneTime;
    private GeneratedValue<Boolean> resize;
    private GeneratedValue<Boolean> selective;
    private GeneratedValue<String> placeholder;

    public DynamicKeyboard(List<DynamicRow> rows) {
        this.rows = new ArrayList<>(rows);
    }

    public DynamicKeyboard(){
        this.rows = new ArrayList<>();
    }

    /**
    * Creates an InlineKeyboardMarkup object using contained rows and a given <code>ResourcePool</code>.
    * 
    * @param pool The ResourcePool object to use for creating the InlineKeyboardMarkup.
    * @return The created InlineKeyboardMarkup object.
    * @see org.telegram.telegrise.core.ResourcePool
    */
    public InlineKeyboardMarkup createInline(ResourcePool pool){
        return new InlineKeyboardMarkup(rows.stream()
                .filter(r -> r.getWhen().generate(pool))
                .map(r -> r.createInlineRow(pool))
                .filter(r -> !r.isEmpty())
                .collect(Collectors.toList()));
    }

    /**
    * Creates an ReplyKeyboardMarkup object using contained rows, keyboard properties and a given <code>ResourcePool</code>.
    * 
    * @param pool The ResourcePool object to use for creating the ReplyKeyboardMarkup.
    * @return The created ReplyKeyboardMarkup object.
    * @see org.telegram.telegrise.core.ResourcePool
    */
    public ReplyKeyboardMarkup createReply(ResourcePool pool){
        return ReplyKeyboardMarkup.builder()
                .keyboard(rows.stream()
                    .filter(r -> r.getWhen().generate(pool))
                    .map(r -> r.createKeyboardRow(pool))
                    .filter(r -> !r.isEmpty()).collect(Collectors.toList()))
                .isPersistent(isPersistent != null ? isPersistent.generate(pool) : null)
                .resizeKeyboard(resize != null ? resize.generate(pool) : null)
                .selective(selective != null ? selective.generate(pool) : null)
                .oneTimeKeyboard(oneTime != null ? oneTime.generate(pool) : null)
                .inputFieldPlaceholder(placeholder != null ? placeholder.generate(pool) : null)
                .build();
    }

    /**
     * Finds a switch button object with given key. 
     * Provided key can be a name of switch button or a callback data it produces, such as <code>"[name]-on"</code> or <code>"[name]-off"</code>.
     * 
     * @param key Key related to the target switch
     * @return Switch button object or null if none found
     * @see SwitchButton
     */
    public SwitchButton getSwitch(String key){
        if (this.switches.containsKey(key)) return this.switches.get(key);
        else {
            String modifiedKey = key.substring(0, key.length() - SwitchButton.ON_SUFFIX.length());
            if (key.endsWith(SwitchButton.ON_SUFFIX) && this.switches.containsKey(modifiedKey))
                return this.switches.get(modifiedKey);

            modifiedKey = key.substring(0, key.length() - SwitchButton.OFF_SUFFIX.length());
            if (key.endsWith(SwitchButton.OFF_SUFFIX) && this.switches.containsKey(modifiedKey))
                return this.switches.get(modifiedKey);
        }

        return null;
    }


    @Deprecated
    public void reloadSwitches(){
        switches.clear();
        this.rows.stream().map(DynamicRow::getButtons).flatMap(List::stream)
                .filter(SwitchButton.class::isInstance)
                .forEach(s -> this.switches.put(((SwitchButton) s).getName(), (SwitchButton) s));
    }

    /**
     * Finds a switch button object with given query. 
     * 
     * @param query callback query of pressed switch
     * @return Switch button object or null if none found
     * @see SwitchButton
     */
    public SwitchButton getSwitch(CallbackQuery query){
        return this.getSwitch(query.getData());
    }

    /**
    * Returns an immutable copy of switches list.
    * 
    * @return list of SwitchButton objects
    * @see SwitchButton
    */
    public List<SwitchButton> getSwitches(){
        return List.copyOf(this.switches.values());
    }

    /**
     * Retuns a DynamicButton that contained at given row and column.
     * 
     * @param row keyboard's row where to take a button from
     * @param column row's column where to take a button from
     * @return targeted DynamicButton instance
     * @see DynamicButton
     */
    public DynamicButton get(int row, int column){
        return this.rows.get(row).getButtons().get(column);
    }

    /**
     * Enables a button at given row and column. 
     * Enabled buttons will appear in the generated keyboard.
     * 
     * @param row keyboard's row where to pick a button from
     * @param column row's column where to pick a button from 
     * @see DynamicButton
     */
    public void enableButton(int row, int column){
        this.get(row, column).setWhen(GeneratedValue.ofValue(true));
    }

    /**
     * Disables a button at given row and column. 
     * Disabled buttons will not appear in the generated keyboard.
     * 
     * @param row keyboard's row where to pick a button from
     * @param column row's column where to pick a button from 
     * @see DynamicButton
     */
    public void disableButton(int row, int column){
        this.get(row, column).setWhen(GeneratedValue.ofValue(false));
    }

    /**
     * Enables a row at given index. 
     * Enabled row will appear in the generated keyboard.
     * 
     * @param row keyboard's row index
     * @see DynamicButton
     */
    public void enableRow(int row){
        this.rows.get(row).setWhen(GeneratedValue.ofValue(true));
    }

    /**
     * Disables a row at given index. 
     * Disabled row will appear not in the generated keyboard.
     * 
     * @param row keyboard's row index
     * @see DynamicButton
     */
    public void disableRow(int row){
        this.rows.get(row).setWhen(GeneratedValue.ofValue(false));
    }
}
