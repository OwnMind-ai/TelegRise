package org.telegram.telegrise.core.expressions.tokens;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ClassUtils;

/**
 * Represents a value token. Can be any value, including null.
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class ValueToken implements PrimitiveToken{
    private Object value;
    private Class<?> type;

    @Override
    public TokenTypes getTokenType() {
        return TokenTypes.VALUE;
    }

    @Override
    public String getStringValue() {
        return value instanceof String ? "\"" + value + "\"" :
                value instanceof Character ? "'" + value + "'" :
                value.toString();
    }

    public Object getValue(Class<?> returnType) {
        if (value == null) return null;

        if (returnType.isAssignableFrom(value.getClass())) {
            return returnType.cast(value);
        } else if (ClassUtils.isPrimitiveOrWrapper(returnType)) {
            return switch (returnType.getName()) {
                case "byte", "java.lang.Byte" -> ((Number) value).byteValue();
                case "short", "java.lang.Short" -> ((Number) value).shortValue();
                case "int", "java.lang.Integer" -> ((Number) value).intValue();
                case "long", "java.lang.Long" -> ((Number) value).longValue();
                case "char", "java.lang.Character" -> (char) ((Number) value).intValue();
                case "float", "java.lang.Float" -> ((Number) value).floatValue();
                case "double", "java.lang.Double" -> ((Number) value).doubleValue();
                case "boolean", "java.lang.Boolean" -> (boolean) value;
                default -> throw new IllegalArgumentException("Cannot convert " + type.getName() + " to " + returnType.getName());
            };
        } if (returnType.isAssignableFrom(String.class)) {
            return value.toString();
        } else {
            throw new IllegalArgumentException("Cannot convert " + type.getName() + " to " + returnType.getName());
        }
    }
}
