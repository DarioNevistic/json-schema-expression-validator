package com.dnevi.expression.validator;

import com.dnevi.expression.validator.expression.ExpressionValidator;
import com.dnevi.expression.validator.schema.StateSchema;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class ValidatorApplication {

    /**
     * Example use of expression validator. To do - Implement CLI arguments parser.
     */
    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = ValidatorApplication.class
                .getResourceAsStream("json-schema-classpath");
        StateSchema schema = mapper.readValue(inputStream, new TypeReference<>() {
        });

        var expression = "A valid json schema expression.";
        var validator = new ExpressionValidator();
        validator.validate(expression, schema);
    }
}
