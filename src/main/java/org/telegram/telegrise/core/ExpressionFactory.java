package org.telegram.telegrise.core;

import java.text.NumberFormat;
import java.text.ParseException;

public class ExpressionFactory {
    public static <T> GeneratedValue<T> parseExpression(String text, Class<T> type) {
        if (Number.class.isAssignableFrom(type)) {
            try {
                return GeneratedValue.ofValue(type.cast(NumberFormat.getInstance().parse(text)));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        } else if (Boolean.class.isAssignableFrom(type)) {
            return GeneratedValue.ofValue(type.cast(text.equals("true")));
        } else if (String.class.isAssignableFrom(type)) {
            return GeneratedValue.ofValue(type.cast(text));
        } else {
            throw new UnsupportedOperationException();  //TODO
        }
    }
}
