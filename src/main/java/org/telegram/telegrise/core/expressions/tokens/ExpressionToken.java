package org.telegram.telegrise.core.expressions.tokens;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class ExpressionToken implements Token{
    private Token left, right;
    private OperatorToken operatorToken;

    @Override
    public TokenTypes getTokenType() {
        return TokenTypes.EXPRESSION;
    }
}
