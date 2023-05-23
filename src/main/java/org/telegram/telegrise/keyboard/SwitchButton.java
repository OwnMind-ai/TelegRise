package org.telegram.telegrise.keyboard;

import lombok.Getter;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.keyboard.Switch;

/**
 * Represents a dynamic switch that used in <a href="#{@link}">{@link org.telegram.telegrise.keyboard.DynamicKeyboard DynamicKeyboard}</a>.
 * Switch buttons contain two button states that replace each other when interacting with them. 
 * <p>
 * After interacting with the switch, it changes its callback data using folowing pattern (replace square brackets with corresponding values):
 * <code>[SWITCH_NAME]-[on/off]</code>. This callback data indecates next switch state after interacting with it, 
 * so that enabled switch will produce <code>[SWITCH_NAME]-off</code> and disabled will produce <code>[SWITCH_NAME]-on</code>.
 * 
 * @see DynamicButton
 * @since 0.4
 */
public final class SwitchButton extends DynamicButton{
    /**
    * Creates a switch button from a <a href="#{@link}">{@link org.telegram.telegrise.core.elements.keyboard.Switch Switch}</a> object.
    * 
    * @param button The Switch object to create a dynamic button from.
    * @param pool The ResourcePool object used to determine initial switch button state
    * @return The created SwitchButton object.
    */
    public static SwitchButton ofSwitch(Switch button, ResourcePool pool){
        SwitchButton switchButton = new SwitchButton(
                button.getOnState(),
                button.getOffState(),
                button.getName(),
                button.getEnabled().generate(pool)
        );

        switchButton.setWhen(button.getWhen());
        return switchButton;
    }

    public static final String OFF_SUFFIX = "-off";
    public static final String ON_SUFFIX = "-on";

    @Getter
    private final GeneratedValue<String> onState;
    @Getter
    private final GeneratedValue<String> offState;
    @Getter
    private final String name;
    @Getter
    private boolean enabled;

    public SwitchButton(GeneratedValue<String> onState, GeneratedValue<String> offState, String name) {
        this.onState = onState;
        this.offState = offState;
        this.name = name;

        this.setText(offState);
        this.setCallbackData(GeneratedValue.ofValue(this.name + OFF_SUFFIX));
    }

    public SwitchButton(GeneratedValue<String> onState, GeneratedValue<String> offState, String name, boolean enabled) {
        this.onState = onState;
        this.offState = offState;
        this.name = name;
        this.enabled = enabled;

        this.setText(enabled ? onState : offState);
        this.setCallbackData(GeneratedValue.ofValue(this.getNextData(this.enabled)));
    }

    /**
     * Returns next callback data according to the given state.
     * 
     * @param state switch state
     * @return next callback data string
     */
    private String getNextData(boolean state) {
        return this.name + (!state ? ON_SUFFIX : OFF_SUFFIX);
    }

    /**
     * Toggles the switch state as well as it's next text and callback data.
     * 
     * @param pool The ResourcePool object used to generate next button text
     */
    public void flip(ResourcePool pool){
        this.enabled = !enabled;
        this.setCallbackData(GeneratedValue.ofValue(this.getNextData(this.enabled)));
        this.setText(GeneratedValue.ofValue(this.enabled ? this.onState.generate(pool) : this.offState.generate(pool)));
    }

    /**
     * Sets the switch state to a given value as well as it's next text and callback data.
     * 
     * @param state boolean value of switch state
     * @param pool The ResourcePool object used to generate next button text
     */
    public void setEnabled(boolean state, ResourcePool pool){
        this.enabled = state;
        this.setCallbackData(GeneratedValue.ofValue(this.getNextData(this.enabled)));
        this.setText(GeneratedValue.ofValue(this.enabled ? this.onState.generate(pool) : this.offState.generate(pool)));
    }
}
