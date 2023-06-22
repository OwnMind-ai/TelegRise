package org.telegram.telegrise.core.expressions;

import lombok.Getter;

public enum ErrorCodes {
    UNDEFINED_TOKEN("undefined token"),
    UNDEFINED_OPERATOR("undefined operator"),
    ILLEGAL_IF_ARGUMENT("illegal argument for IF construction"),
    MISSING_DO_STATEMENT("IF construction missing DO statement");

    @Getter
    private final String message;

    ErrorCodes(String message) {
        this.message = message;
    }
}
