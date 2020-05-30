package com.dnevi.expression.validator.exception;

public class InvalidStatePathTypeException extends RuntimeException {

    public InvalidStatePathTypeException(String type) {
        super(String.format("State type '%s' is not valid", type));
    }
}
