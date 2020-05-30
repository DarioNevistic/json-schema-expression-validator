package com.dnevi.expression.validator.validation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class Validation implements Serializable {

    private static final long serialVersionUID = 1905122041950251207L;
    private boolean required;

    @JsonCreator
    public Validation(@JsonProperty("required") boolean required) {
        this.required = required;
    }
}
