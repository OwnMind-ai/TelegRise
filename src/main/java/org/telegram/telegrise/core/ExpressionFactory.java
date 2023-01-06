package org.telegram.telegrise.core;

public class ExpressionFactory {
    public static GeneratedValue<?> parseExpression(String text){
        return GeneratedValue.ofValue(text); //TODO
    }
}
