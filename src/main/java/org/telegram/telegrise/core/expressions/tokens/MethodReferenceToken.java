package org.telegram.telegrise.core.expressions.tokens;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor
public class MethodReferenceToken implements Token {
    private String method;
    private List<String> params;

    @Override
    public TokenTypes getTokenType() {
        return TokenTypes.REFERENCE;
    }
}
