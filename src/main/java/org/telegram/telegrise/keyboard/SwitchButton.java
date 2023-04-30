package org.telegram.telegrise.keyboard;

import lombok.Getter;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.keyboard.Switch;

public final class SwitchButton extends DynamicButton{
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

    private String getNextData(boolean state) {
        return this.name + (!state ? ON_SUFFIX : OFF_SUFFIX);
    }

    public void flip(ResourcePool pool){
        this.enabled = !enabled;
        this.setCallbackData(GeneratedValue.ofValue(this.getNextData(this.enabled)));
        this.setText(GeneratedValue.ofValue(this.enabled ? this.onState.generate(pool) : this.offState.generate(pool)));
    }

    public void setEnabled(boolean state, ResourcePool pool){
        this.enabled = state;
        this.setCallbackData(GeneratedValue.ofValue(this.getNextData(this.enabled)));
        this.setText(GeneratedValue.ofValue(this.enabled ? this.onState.generate(pool) : this.offState.generate(pool)));
    }
}
