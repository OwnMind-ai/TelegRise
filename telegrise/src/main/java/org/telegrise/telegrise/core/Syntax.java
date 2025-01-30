package org.telegrise.telegrise.core;

import java.util.List;

public final class Syntax {
    public static final String EXPRESSION_START = "${";
    public static final String EXPRESSION_END = "}";
    public static final String LIST_SPLITERATOR = "(?<!\\\\);";
    public static final String IMPORT = "import";

    public static final String METHOD_REFERENCE_START = "#";
    public static final String GENERATOR_START = "::";
    public static final String PARALLEL_SEPARATOR = ";";
    public static final String LIST_SEPARATOR = ",";
    public static final String CHAIN_SEPARATOR = "->";
    public static final String AND_OPERATOR = "AND";
    public static final String OR_OPERATOR = "OR";
    public static final String EQUALS_OPERATOR = "==";
    public static final String NOT_EQUALS_OPERATOR = "!=";
    public static final String GREATER_OPERATOR = ">";
    public static final String GREATER_OR_EQUALS_OPERATOR = ">=";
    public static final String LESS_OR_EQUALS_OPERATOR = "<=";
    public static final String LESS_OPERATOR = "<";
    public static final List<String> OPERATORS = List.of(
            PARALLEL_SEPARATOR, CHAIN_SEPARATOR, LIST_SEPARATOR, AND_OPERATOR, OR_OPERATOR, EQUALS_OPERATOR,
            NOT_EQUALS_OPERATOR, GREATER_OPERATOR, LESS_OPERATOR, GREATER_OR_EQUALS_OPERATOR, LESS_OR_EQUALS_OPERATOR
    );
    public static final int MAX_OPERATORS_LENGTH = 3;

    public static final String PARENTHESES_START = "(";
    public static final String PARENTHESES_END = ")";

    public static final String IF = "IF";
    public static final String DO = "DO";
    public static final String ELSE = "ELSE";
    public static final List<String> KEYWORDS = List.of(IF, DO, ELSE);
    public static final int MAX_KEYWORDS_LENGTH = KEYWORDS.stream().mapToInt(String::length).max().orElse(0);

    public static final String REGISTER = "register";
}
