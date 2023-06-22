package org.telegram.telegrise.core.expressions.tokens;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class KeywordToken implements Token{
    private String keyword;

    @Override
    public TokenTypes getTokenType() {
        return TokenTypes.KEYWORD;
    }
}
