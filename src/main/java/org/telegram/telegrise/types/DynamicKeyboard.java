package org.telegram.telegrise.types;

import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.keyboard.Keyboard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class DynamicKeyboard implements Serializable {
    public static DynamicKeyboard ofKeyboard(Keyboard keyboard){
        DynamicKeyboard dynamicKeyboard = new DynamicKeyboard();

        dynamicKeyboard.getRows().addAll(keyboard.getRows().stream().map(row -> {
            DynamicRow dynamicRow = DynamicRow.ofRow(row);
            dynamicRow.getButtons().stream().filter(SwitchButton.class::isInstance)
                    .forEach(s -> dynamicKeyboard.switches.put(((SwitchButton) s).getName(), (SwitchButton) s));

            return dynamicRow;
        }).collect(Collectors.toList()));

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

    public InlineKeyboardMarkup createInline(ResourcePool pool){
        return new InlineKeyboardMarkup(rows.stream()
                .filter(r -> r.getWhen().generate(pool))
                .map(r -> r.createInlineRow(pool))
                .filter(r -> !r.isEmpty())
                .collect(Collectors.toList()));
    }

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

    public SwitchButton getSwitch(CallbackQuery query){
        return this.switches.get(query.getData());
    }

    public DynamicButton get(int row, int column){
        return this.rows.get(row).getButtons().get(column);
    }

    public void enableButton(int row, int column){
        this.get(row, column).setWhen(GeneratedValue.ofValue(true));
    }

    public void disableButton(int row, int column){
        this.get(row, column).setWhen(GeneratedValue.ofValue(false));
    }

    public void enableRow(int row){
        this.rows.get(row).setWhen(GeneratedValue.ofValue(true));
    }

    public void disableRow(int row){
        this.rows.get(row).setWhen(GeneratedValue.ofValue(false));
    }
}
