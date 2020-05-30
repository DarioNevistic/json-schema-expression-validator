package com.dnevi.expression.validator.exception;

public class UnknownExpressionTypeException extends RuntimeException {

    public UnknownExpressionTypeException(String type, String expression) {
        super(String.format("Malformed expression '%s'. Unknown type '%s'.", expression, type));
    }
}
