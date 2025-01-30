package org.telegrise.telegrise.core.expressions.tokens;

public interface MethodContainer {
    String getClassName();
    String getMethod();
    boolean isStatic();
}
