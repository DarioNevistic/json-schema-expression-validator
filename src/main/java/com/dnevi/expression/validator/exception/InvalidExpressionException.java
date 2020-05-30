package com.dnevi.expression.validator.exception;

public class InvalidExpressionException extends RuntimeException {

    public InvalidExpressionException(String expression) {
        super(String.format("Malformed expression '%s'. Parenthesis is not matching.", expression));
    }
}