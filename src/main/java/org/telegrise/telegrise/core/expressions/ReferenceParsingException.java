package org.telegrise.telegrise.core.expressions;

import lombok.Getter;

@Getter
public class ReferenceParsingException extends Exception{
    private final ErrorCodes errorCode;
    private final int position;

    public ReferenceParsingException(ErrorCodes errorCode, int position) {
        this.errorCode = errorCode;
        this.position = position;
    }

    @Override
    public String toString() {
        return "Error occurred while parsing method reference: " + this.errorCode.getMessage();
    }
}
