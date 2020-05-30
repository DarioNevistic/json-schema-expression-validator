package com.dnevi.expression.validator.schema;

import com.dnevi.expression.validator.validation.Validation;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({@JsonSubTypes.Type(value = ObjectSchema.class, name = "OBJECT"),
        @JsonSubTypes.Type(value = IntegerSchema.class, name = "INTEGER"),
        @JsonSubTypes.Type(value = NumberSchema.class, name = "NUMBER"),
        @JsonSubTypes.Type(value = BooleanSchema.class, name = "BOOLEAN"),
        @JsonSubTypes.Type(value = ArraySchema.class, name = "ARRAY"),
        @JsonSubTypes.Type(value = StringSchema.class, name = "STRING")})
@Getter
public abstract class Schema implements Serializable {

    @JsonIgnore
    protected String id;
    @Setter
    @JsonIgnore
    protected String parentId;
    @JsonIgnore
    protected Boolean isRoot = false;
    protected Validation validation;

    private String type;

    void markAsRoot() {
        this.isRoot = true;
    }

    public abstract void setId(String id);

    public abstract void accept(Visitor visitor);
}
