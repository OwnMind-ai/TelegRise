package org.telegrise.telegrise.transition;

public record ExecutionOptions(Boolean execute, String edit, String source, boolean ignoreError) {
    public static final String EDIT_FIRST = "first";

    public static ExecutionOptions always(){
        return new ExecutionOptions(true, null, null, false);
    }
}
