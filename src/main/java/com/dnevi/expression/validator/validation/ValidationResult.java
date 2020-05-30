package com.dnevi.expression.validator.validation;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ValidationResult {

    private List<ValidationError> validationErrors;

    ValidationResult() {
        this.validationErrors = new ArrayList<>();
    }

    void addError(ValidationError error) {
        this.validationErrors.add(error);
    }

    public boolean hasErrors() {
        return !this.getValidationErrors().isEmpty();
    }

}
