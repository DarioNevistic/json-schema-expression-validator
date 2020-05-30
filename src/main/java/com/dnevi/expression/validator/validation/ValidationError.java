package com.dnevi.expression.validator.validation;

import lombok.Getter;

@Getter
public class ValidationError {
    private String message;

    public ValidationError(String message) {
        this.message = message;
    }
}
