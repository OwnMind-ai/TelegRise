package org.telegram.telegrise.core.expressions.tokens;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor
public class MethodReferenceToken implements Token {
    private String className;
    private String method;
    private List<String> params;

    public MethodReferenceToken(String method, List<String> params){
        this.method = method;
        this.params = params;
    }

    public boolean isStatic(){
        return className != null;
    }

    @Override
    public TokenTypes getTokenType() {
        return TokenTypes.REFERENCE;
    }
}
