package org.telegram.telegrise.core;

import java.util.function.Predicate;

public class ExpressionFactory {
    public static GeneratedValue<?> parseExpression(String text){
        return GeneratedValue.ofValue(text); //TODO
    }
}
