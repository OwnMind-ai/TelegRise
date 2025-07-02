package org.telegrise.telegrise.keyboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.base.BranchingElement;
import org.telegrise.telegrise.core.elements.keyboard.Keyboard;
import org.telegrise.telegrise.core.elements.keyboard.Switch;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This class stores the state of keyboard that attached to displayed {@link BranchingElement BranchingElement}.
 * The state includes dynamically disabled
 * and enabled rows and buttons as well as current state of {@link Switch switches}.
 *
 * @see Keyboard
 * @since 0.9
 */
@SuppressWarnings("UnusedReturnValue")
public final class KeyboardState {
    public static final String SWITCH_ENABLED = "enabled";
    public static final String SWITCH_DISABLED = "disabled";

    @Getter
    private final BranchingElement parent;
    private final ButtonData[][] buttonsData;
    private final Map<String, String> switchStates = new HashMap<>();   // TODO add carousel switches

    @ApiStatus.Internal
    public KeyboardState(BranchingElement parent, Keyboard keyboard, ResourcePool pool){
        this.parent = parent;

        this.switchStates.putAll(keyboard.getRows().stream()
                .flatMap(r -> r.getButtons().stream())
                .filter(Switch.class::isInstance).map(Switch.class::cast)
                .collect(Collectors.toMap(Switch::getName, 
                    b -> b.getInitial().generate(pool) ? SWITCH_ENABLED : SWITCH_DISABLED))
        );

        this.buttonsData = keyboard.getRows().stream()
                .map(r -> r.getButtons().stream()
                        .map(b -> {
                            var text =  b.getText().isStatic() ? b.getText().generate(null) : null;
                            var callback = b.getCallbackData() != null && b.getCallbackData().isStatic() ?
                                    b.getCallbackData().generate(null) : null;

                            return new ButtonData(true, text, callback);
                        }).toArray(ButtonData[]::new)
                ).toArray(ButtonData[][]::new);
    }

    /**
     * Returns current state of switch named <code>name</code> in boolean.
     * Can be used only for switches in default mode.
     *
     * @param name switch name
     * @return true if switch is in "on" state
     */
    public boolean isSwitchEnabled(String name) {
        return SWITCH_ENABLED.equals(getSwitchValue(name));
    }

     /**
     * Returns current state of switch named <code>name</code>.
     * If switch is in default mode,
     * method returns either {@code KeyboardState.SWITCH_ENABLED} or {@code KeyboardState.SWITCH_DISABLED}.
     * If switch in carousel mode, the method returns the current state name on carousel.
     *
     * @param name switch name
     * @return current state
     */
    public String getSwitchValue(String name){
        return switchStates.get(name);
    }

    /**
     * Changes state of the switch name {@code name} to the next one.
     * @param name switch name
     */
    public void flipSwitch(String name){
        switchStates.put(name, switchStates.get(name).equals(SWITCH_ENABLED) ? SWITCH_DISABLED : SWITCH_ENABLED);
    }

    /**
     * Hides button at specific {@code row} and {@code column}. Buttons disabled this way will omit 'when' check.
     *
     * @param row keyboard row
     * @param column index of a button in the row
     */
    public void disableButton(int row, int column){
        buttonsData[row][column].setVisible(false);
    }

    /**
     * Enables button at specific {@code row} and {@code column}. Buttons will use 'when' check to be hidden.
     *
     * @param row keyboard row
     * @param column index of a button in the row
     */
    public void enableButton(int row, int column){
        buttonsData[row][column].setVisible(true);
    }

    /**
     * Returns true, if button at specific {@code row} and {@code column} is enabled (by default, returns true).
     *
     * @param row keyboard row
     * @param column index of a button in the row
     * @return current status
     */
    public boolean isEnabled(int row, int column){
        return buttonsData[row][column].visible;
    }

    /**
     * Hides all buttons at specific {@code row}. Buttons disabled this way will omit 'when' check.
     *
     * @param row keyboard row
     */
    public void disableRow(int row){
        for (ButtonData data : buttonsData[row]){
            data.setVisible(false);
        }
    }

    /**
     * Enables all buttons at specific {@code row}. Buttons will use 'when' check to be hidden.
     *
     * @param row keyboard row
     */
    public void enableRow(int row){
        for (ButtonData data : buttonsData[row]){
            data.setVisible(true);
        }
    }

    /**
     * Enables all buttons that have the same button text as specified.
     * Buttons with data generated by expression or method reference are not included.
     * @param text data to compare with
     * @return the number of buttons changed
     */
    public int enableButtonsOfText(String text) {
        return traverseButtons(d -> text.equals(d.getText()), this::enableButton);
    }

    /**
     * Enables all buttons that have the same callback data as specified.
     * Buttons with data generated by expression or method reference are not included.
     * @param callbackData data to compare with
     * @return the number of buttons changed
     */
    public int enableButtonsOfCallback(String callbackData) {
        return traverseButtons(d -> callbackData.equals(d.getCallback()), this::enableButton);
    }

    /**
     * Enables all buttons which data satisfy the specified predicate.
     * Buttons with data generated by expression or method reference are not included.
     * @param predicate predicate to use
     * @return the number of buttons changed
     */
    public int enableButtonsOfData(Predicate<ButtonData> predicate) {
        return traverseButtons(predicate, this::enableButton);
    }

    /**
     * Disables all buttons that have the same button text as specified.
     * Buttons with data generated by expression or method reference are not included.
     * @param text data to compare with
     * @return the number of buttons changed
     */
    public int disableButtonsOfText(String text) {
        return traverseButtons(d -> text.equals(d.getText()), this::disableButton);
    }

    /**
     * Disables all buttons that have the same callback data as specified.
     * Buttons with data generated by expression or method reference are not included.
     * @param callbackData data to compare with
     * @return the number of buttons changed
     */
    public int disableButtonsOfCallback(String callbackData) {
        return traverseButtons(d -> callbackData.equals(d.getCallback()), this::disableButton);
    }

    /**
     * Disables all buttons which data satisfy the specified predicate.
     * Buttons with data generated by expression or method reference are not included.
     * @param predicate predicate to use
     * @return the number of buttons changed
     */
    public int disableButtonsOfData(Predicate<ButtonData> predicate) {
        return traverseButtons(predicate, this::disableButton);
    }

    private int traverseButtons(Predicate<ButtonData> predicate, BiConsumer<Integer, Integer> consumer){
        int counter = 0;
        for (int r = 0, lr = buttonsData.length; r < lr; r++) {
            ButtonData[] row = buttonsData[r];
            for (int b = 0, lb = row.length; b < lb; b++) {
                ButtonData button = row[b];
                if (button != null && predicate.test(button)) {
                    consumer.accept(r, b);
                    counter++;
                }
            }
        }

        return counter;
    }

    @Data @AllArgsConstructor
    public static final class ButtonData {
        private boolean visible;
        private @Nullable String text;
        private @Nullable String callback;
    }
}
