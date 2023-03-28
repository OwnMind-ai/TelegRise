package org.telegram.telegrise.types;

import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.keyboard.Keyboard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class DynamicKeyboard implements Serializable {
    public static DynamicKeyboard ofKeyboard(Keyboard keyboard){
        return new DynamicKeyboard(keyboard.getRows().stream().map(DynamicRow::ofRow).collect(Collectors.toList()));
    }

    @Getter
    private final List<DynamicRow> rows;

    public DynamicKeyboard(List<DynamicRow> rows) {
        this.rows = new ArrayList<>(rows);
    }

    public DynamicKeyboard(){
        this.rows = new ArrayList<>();
    }

    public InlineKeyboardMarkup createInline(ResourcePool pool){
        return new InlineKeyboardMarkup(rows.stream()
                .filter(r -> r.getWhen().generate(pool))
                .map(r -> r.createInlineRow(pool)).collect(Collectors.toList()));
    }

    public ReplyKeyboardMarkup createReply(ResourcePool pool){
        return new ReplyKeyboardMarkup(rows.stream()
                .filter(r -> r.getWhen().generate(pool))
                .map(r -> r.createKeyboardRow(pool)).collect(Collectors.toList()));
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
