package com.dnevi.expression.validator.schema;

import com.dnevi.expression.validator.validation.Validation;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class IntegerSchema extends Schema implements Serializable {

    @JsonProperty("const")
    private Integer constant;

    @JsonCreator
    public IntegerSchema(@JsonProperty("const") Integer constant,
            @JsonProperty("validation") Validation validation) {
        this.constant = constant;
        this.validation = validation;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitIntegerSchema(this);
    }
}
