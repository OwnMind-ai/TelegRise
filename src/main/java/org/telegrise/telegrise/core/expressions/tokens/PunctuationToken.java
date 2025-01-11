package org.telegrise.telegrise.core.expressions.tokens;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class PunctuationToken implements Token{
    private String value;

    @Override
    public TokenTypes getTokenType() {
        return TokenTypes.PUNCTUATION;
    }
}
