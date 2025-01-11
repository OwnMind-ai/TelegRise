package org.telegrise.telegrise.core.expressions.tokens;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReferenceGeneratorToken implements Token, MethodContainer{
    private String className;
    private String method;
    private List<PrimitiveToken> params;

    public ReferenceGeneratorToken(String method, List<PrimitiveToken> params) {
        this.method = method;
        this.params = params;
    }

    public boolean isStatic(){ return className != null; }

    @Override
    public TokenTypes getTokenType() {
        return TokenTypes.GENERATOR;
    }
}
