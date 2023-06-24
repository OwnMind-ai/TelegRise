package org.telegram.telegrise.core.expressions;

import lombok.Getter;

public class ReferenceParsingException extends Exception{
    @Getter
    private final ErrorCodes errorCode;
    @Getter
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
