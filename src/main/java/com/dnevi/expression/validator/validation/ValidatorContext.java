package com.dnevi.expression.validator.validation;

import lombok.Getter;

@Getter
public class ValidatorContext {

    private ValidationResult validationResult;

    public ValidatorContext() {
        this.validationResult = new ValidationResult();
    }

    public void addError(ValidationError result) {
        this.validationResult.addError(result);
    }
}
