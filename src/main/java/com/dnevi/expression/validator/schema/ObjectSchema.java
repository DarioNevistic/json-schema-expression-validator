package com.dnevi.expression.validator.schema;

import com.dnevi.expression.validator.validation.Validation;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.io.Serializable;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class ObjectSchema extends Schema implements Serializable, NestableSchema {

    private Map<String, Schema> properties;

    @JsonCreator
    public ObjectSchema(@JsonProperty("properties") Map<String, Schema> properties,
            @JsonProperty("validation") Validation validation) {
        this.properties = properties;
        this.validation = validation;
    }

    public void setId(String id) {
        this.id = id;
        if (this.properties != null) {
            this.properties.forEach((k, v) -> {
                v.setId(id.concat("/").concat(k));
                v.setParentId(this.getId());
            });
        }
    }

    @JsonIgnore
    public Map<String, Schema> getChildSchemas() {
        return this.getProperties();
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitObjectSchema(this);
    }
}
