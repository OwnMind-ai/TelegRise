package org.telegrise.telegrise.core.expressions.tokens;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class RawToken implements PrimitiveToken {
    private String value;

    @Override
    public TokenTypes getTokenType() {
        return TokenTypes.RAW;
    }

    @Override
    public String getStringValue() {
        return value;
    }
}
