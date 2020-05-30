package com.dnevi.expression.validator.exception;

public class InvalidStatePathStringException extends RuntimeException {

    public InvalidStatePathStringException(String path) {
        super(String.format("Path '%s' is not valid", path));
    }
}

