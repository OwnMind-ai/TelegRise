package org.telegrise.telegrise.bot.controllers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegrise.telegrise.annotations.Reference;
import org.telegrise.telegrise.annotations.TreeController;
import org.telegrise.telegrise.builtin.DefaultController;
import org.telegrise.telegrise.caching.CachingStrategy;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiFunction;

@TreeController
public class CalculatorController extends DefaultController {
    private String value = "0";
    private String input = "0";
    @Reference
    private Operations operation;

    @Reference
    public void solve(){
        BigDecimal left = new BigDecimal(value, MathContext.DECIMAL128);
        BigDecimal right = new BigDecimal(input, MathContext.DECIMAL128);
        Operations operation = this.operation;

        allClear();

        if (operation == Operations.DIVIDE && right.equals(BigDecimal.ZERO))
            value = "Error";
        else
            value = operation.function.apply(left, right).toPlainString();
    }

    @Reference
    public void toPercent(){
        var target = getTarget();
        target.set(new BigDecimal(target.get()).divide(new BigDecimal(100), MathContext.DECIMAL128).toPlainString());
    }

    @Reference
    public void changeSign(){
        var target = getTarget();
        target.set( target.get().startsWith("-") ? target.get().substring(1) : "-" + target.get());
    }

    @Reference(caching = CachingStrategy.UPDATE)
    public Operations getInputOperation(Update update){
        return Optional.ofNullable(update.getCallbackQuery()).map(CallbackQuery::getData).filter(d -> d.length() == 1)
                .flatMap(d -> Arrays.stream(Operations.values()).filter(o -> o.button.equals(d)).findFirst())
                .orElse(null);
    }

    @Reference
    public boolean isClearAll(){
        return this.input.equals("0");
    }

    @Reference
    public void setOperation(Operations operation){
        if (value.equals("Error")) value = "0";
        if (this.operation != null) solve();

        this.operation = operation;
        this.input = "0";
    }

    @Reference(caching = CachingStrategy.UPDATE)
    public String getInputDigit(Update update){
        return Optional.ofNullable(update.getCallbackQuery()).map(CallbackQuery::getData)
                .filter(d -> d.length() == 1 && Character.isDigit(d.charAt(0))).orElse(null);
    }

    @Reference
    public void typeDigit(String digit){
        boolean special = digit.equals(".") || digit.equals("00");
        var target = getTarget();
        if (target.get().equals("Error")) target.set("0");

        target.set((special || !target.get().equals("0") ? target.get() : "") + getTypeCharacter(target.get(), digit));
    }

    private String getTypeCharacter(String value, String digit) {
        return switch (digit) {
            case "." -> value.contains(".") ? "" : ".";
            case "00" -> value.equals("0") ? "" : "00";
            default -> digit;
        };
    }

    private Target getTarget(){
        if (operation == null) return new Target() {
            @Override public void set(String v) { value = v ;}
            @Override public String get() { return value; }
        };
        return new Target() {
            @Override public void set(String v) { input = v ;}
            @Override public String get() { return input; }
        };
    }

    @Reference
    public String getDisplay(){
        return value + (operation == null ? "" : " %s <u>%s</u>".formatted(operation.button, input));
    }

    @Reference
    public void clear(){
        this.input = "0";
    }

    @Reference
    public void allClear(){
        value = "0";
        input = "0";
        operation = null;
    }

    @AllArgsConstructor @Getter
    public enum Operations{
        ADD("+", (f, s) -> f.add(s, MathContext.DECIMAL128)),
        SUBTRACT("-", (f, s) -> f.subtract(s, MathContext.DECIMAL128)),
        MULTIPLY("×", (f, s) -> f.multiply(s, MathContext.DECIMAL128)),
        DIVIDE("÷", (f, s) -> f.divide(s, MathContext.DECIMAL128));

        private final String button;
        private final BiFunction<BigDecimal, BigDecimal, BigDecimal> function;
    }

    private interface Target{
        void set(String v);
        String get();
    }
}
