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
public class ArraySchema extends Schema implements Serializable {

    private Schema items;

    @JsonCreator
    public ArraySchema(@JsonProperty("items") Schema items,
            @JsonProperty("validation") Validation validation) {
        this.items = items;
        this.validation = validation;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitArraySchema(this);
    }
}
