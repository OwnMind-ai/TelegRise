package org.telegram.telegrise.types;

import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.keyboard.Switch;

public final class SwitchButton extends DynamicButton{
    public static SwitchButton ofSwitch(Switch button){
        return new SwitchButton(
                button.getOnState(),
                button.getOffState(),
                button.getName(),
                button.isEnabled()
        );
    }

    public static final String OFF_SUFFIX = "-off";
    public static final String ON_SUFFIX = "-on";

    @Getter
    private final GeneratedValue<String> onState;
    @Getter
    private final GeneratedValue<String> offState;
    @Getter
    private final String name;
    @Getter @Setter
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

        this.setText(offState);
        this.setCallbackData(GeneratedValue.ofValue(this.name + OFF_SUFFIX));
    }

    private String getNextData(boolean state) {
        return this.name + (!state ? ON_SUFFIX : OFF_SUFFIX);
    }

    public void flip(ResourcePool pool){
        this.enabled = !enabled;
        this.setCallbackData(GeneratedValue.ofValue(this.getNextData(this.enabled)));
        this.setText(GeneratedValue.ofValue(this.enabled ? this.onState.generate(pool) : this.offState.generate(pool)));
    }
}
