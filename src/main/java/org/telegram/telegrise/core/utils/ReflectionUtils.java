package org.telegram.telegrise.core.utils;

import org.telegram.telegrise.annotations.TreeHandler;

import java.lang.StackWalker.StackFrame;

public class ReflectionUtils {
    private static final int WALKER_LIMIT = 6;

    public static Class<?> getCallerTreeClass(){
        StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

        return walker.walk(
                s -> s.limit(WALKER_LIMIT).map(StackFrame::getDeclaringClass)
                        .filter(c -> c.isAnnotationPresent(TreeHandler.class))
                        .findFirst().orElse(null)
        );
    }
}
