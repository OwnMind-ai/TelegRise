package org.telegram.telegrise.core.expressions.tokens;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class OperatorToken implements Token {
    private String operator;

    @Override
    public TokenTypes getTokenType() {
        return TokenTypes.OPERATOR;
    }
}
