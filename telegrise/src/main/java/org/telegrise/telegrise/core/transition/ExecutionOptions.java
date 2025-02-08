package org.telegrise.telegrise.core.transition;

public record ExecutionOptions(Boolean execute, String edit, String source, boolean ignoreError) {
    public static final String EDIT_FIRST = "first";
    //TODO add "last"

    public static ExecutionOptions always(){
        return new ExecutionOptions(true, null, null, false);
    }
}
