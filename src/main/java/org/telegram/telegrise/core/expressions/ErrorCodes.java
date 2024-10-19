package org.telegram.telegrise.core.expressions;

import lombok.Getter;

@Getter
public enum ErrorCodes {
    UNDEFINED_TOKEN("undefined token"),
    UNDEFINED_OPERATOR("undefined operator"),
    ILLEGAL_IF_ARGUMENT("illegal argument for IF construction"),
    MISSING_DO_STATEMENT("IF construction missing DO statement"),
    MISSING_PARAMETERS("must have parameters");

    private final String message;

    ErrorCodes(String message) {
        this.message = message;
    }
}
