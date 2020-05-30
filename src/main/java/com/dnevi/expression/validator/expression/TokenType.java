package com.dnevi.expression.validator.expression;

import java.util.Arrays;
import java.util.List;

public enum TokenType {
    LEFT_PAREN,
    RIGHT_PAREN,

    BANG,
    BANG_EQUAL,
    EQUAL,
    EQUAL_EQUAL,
    GREATER,
    GREATER_EQUAL,
    LESS,
    LESS_EQUAL,

    STRING,
    NUMBER,
    INTEGER,
    JSON_PATH,
    IDENTIFIER,

    AND,
    FALSE,
    NIL,
    OR,
    TRUE,
    BOOLEAN,

    EOF;

    public static List<TokenType> getAllOperators() {
        return Arrays.asList(BANG_EQUAL, EQUAL_EQUAL,
                GREATER, GREATER_EQUAL,
                LESS, LESS_EQUAL, OR, AND);
    }

    public static List<TokenType> getBooleanAndStringOperators() {
        return Arrays.asList(BANG_EQUAL, EQUAL_EQUAL);
    }
}
