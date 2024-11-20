package org.telegram.telegrise.transition;

public record ExecutionOptions(Boolean execute, String edit, String source) {
    public static final String EDIT_FIRST = "first";

    public static ExecutionOptions always(){
        return new ExecutionOptions(true, null, null);
    }
}
