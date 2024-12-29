package org.telegram.telegrise.keyboard;

import lombok.Getter;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.BranchingElement;
import org.telegram.telegrise.core.elements.keyboard.Keyboard;
import org.telegram.telegrise.core.elements.keyboard.Switch;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class KeyboardState {
    public static final String SWITCH_ENABLED = "enabled";
    public static final String SWITCH_DISABLED = "disabled";

    @Getter
    private final BranchingElement parent;
    private final boolean[][] buttonVisibilities;
    private final Map<String, String> switchStates = new HashMap<>();   // TODO add carousel switches

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
    }


    public String getSwitchValue(String name){
        return switchStates.get(name);
    }

    public void flipSwitch(String name){
        switchStates.put(name, switchStates.get(name).equals(SWITCH_ENABLED) ? SWITCH_DISABLED : SWITCH_ENABLED);
    }

    public void disableButton(int row, int column){
        buttonVisibilities[row][column] = false;
    }
    
    public void enableButton(int row, int column){
        buttonVisibilities[row][column] = false;
    }

    public boolean isEnabled(int row, int column){
        return buttonVisibilities[row][column];
    }

    public void disableRow(int row){
        Arrays.fill(buttonVisibilities[row], false);
    }
    
    public void enableRow(int row){
        Arrays.fill(buttonVisibilities[row], true);
    }
}
