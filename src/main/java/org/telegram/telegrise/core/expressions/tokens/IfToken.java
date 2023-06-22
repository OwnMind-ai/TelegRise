package org.telegram.telegrise.core.expressions.tokens;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class IfToken implements Token{
    private Token predicate, doAction, elseAction;

    @Override
    public TokenTypes getTokenType() {
        return TokenTypes.IF_CONSTRUCTION;
    }
}
