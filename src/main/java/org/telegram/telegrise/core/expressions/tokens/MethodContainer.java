package org.telegram.telegrise.core.expressions.tokens;

public interface MethodContainer {
    String getClassName();
    String getMethod();
    boolean isStatic();
}
