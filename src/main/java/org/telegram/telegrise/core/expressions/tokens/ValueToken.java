package org.telegram.telegrise.core.expressions.tokens;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
        return value.toString();
    }
}
