package com.dnevi.expression.validator.schema;

import java.util.Map;

public interface NestableSchema {

    Map<String, Schema> getChildSchemas();

    default void addSchema(String key, Schema schema) {
        this.getChildSchemas().put(key, schema);
    }
}
