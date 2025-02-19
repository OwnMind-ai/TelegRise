package org.telegrise.telegrise.keyboard;

import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.base.BranchingElement;
import org.telegrise.telegrise.core.elements.keyboard.Button;
import org.telegrise.telegrise.core.elements.keyboard.Keyboard;
import org.telegrise.telegrise.core.elements.keyboard.Switch;

import java.util.Arrays;
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
    private final boolean[][] buttonVisibilities;
    private final String[][] buttonsCallbackData;
    private final Map<String, String> switchStates = new HashMap<>();   // TODO add carousel switches

    @ApiStatus.Internal
    public KeyboardState(BranchingElement parent, Keyboard keyboard, ResourcePool pool){
        this.parent = parent;
        this.buttonVisibilities = keyboard.getRows().stream()
                .map(r -> {
                    var a = new boolean[r.getButtons().size()];
                    Arrays.fill(a, true);
                    return a;
                })
                .toArray(boolean[][]::new);

        this.switchStates.putAll(keyboard.getRows().stream()
                .flatMap(r -> r.getButtons().stream())
                .filter(Switch.class::isInstance).map(Switch.class::cast)
                .collect(Collectors.toMap(Switch::getName, 
                    b -> b.getInitial().generate(pool) ? SWITCH_ENABLED : SWITCH_DISABLED))
        );

        this.buttonsCallbackData = keyboard.getRows().stream()
                .map(r -> r.getButtons().stream().map(Button::getCallbackData)
                        .map(d -> d != null && d.isStatic() ? d.generate(pool) : null)
                        .toArray(String[]::new)
                ).toArray(String[][]::new);

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
        buttonVisibilities[row][column] = false;
    }

    /**
     * Enables button at specific {@code row} and {@code column}. Buttons will use 'when' check to be hidden.
     *
     * @param row keyboard row
     * @param column index of a button in the row
     */
    public void enableButton(int row, int column){
        buttonVisibilities[row][column] = false;
    }

    /**
     * Returns true, if button at specific {@code row} and {@code column} is enabled (by default returns true).
     *
     * @param row keyboard row
     * @param column index of a button in the row
     * @return current status
     */
    public boolean isEnabled(int row, int column){
        return buttonVisibilities[row][column];
    }

    /**
     * Hides all buttons at specific {@code row}. Buttons disabled this way will omit 'when' check.
     *
     * @param row keyboard row
     */
    public void disableRow(int row){
        Arrays.fill(buttonVisibilities[row], false);
    }

    /**
     * Enables all buttons at specific {@code row}. Buttons will use 'when' check to be hidden.
     *
     * @param row keyboard row
     */
    public void enableRow(int row){
        Arrays.fill(buttonVisibilities[row], true);
    }

    /**
     * Enables all buttons that have the same callback data as specified.
     * Buttons with data generated by expression or method reference are not included.
     * @param callbackData data to compare with
     * @return the number of buttons changed
     */
    public int enableButtonsOfData(String callbackData) {
        return traverseButtons(callbackData::equals, this::enableButton);
    }

    /**
     * Enables all buttons which callback data satisfy the specified predicate.
     * Buttons with data generated by expression or method reference are not included.
     * @param predicate predicate to use
     * @return the number of buttons changed
     */
    public int enableButtonsOfData(Predicate<String> predicate) {
        return traverseButtons(predicate, this::enableButton);
    }

    /**
     * Disables all buttons that have the same callback data as specified.
     * Buttons with data generated by expression or method reference are not included.
     * @param callbackData data to compare with
     * @return the number of buttons changed
     */
    public int disableButtonsOfData(String callbackData) {
        return traverseButtons(callbackData::equals, this::disableButton);
    }

    /**
     * Disables all buttons which callback data satisfy the specified predicate.
     * Buttons with data generated by expression or method reference are not included.
     * @param predicate predicate to use
     * @return the number of buttons changed
     */
    public int disableButtonsOfData(Predicate<String> predicate) {
        return traverseButtons(predicate, this::disableButton);
    }

    private int traverseButtons(Predicate<String> predicate, BiConsumer<Integer, Integer> consumer){
        int counter = 0;
        for (int r = 0, lr = buttonsCallbackData.length; r < lr; r++) {
            String[] row = buttonsCallbackData[r];
            for (int b = 0, lb = row.length; b < lb; b++) {
                String button = row[b];
                if (button != null && predicate.test(button)) {
                    consumer.accept(r, b);
                    counter++;
                }
            }
        }

        return counter;
    }
}
