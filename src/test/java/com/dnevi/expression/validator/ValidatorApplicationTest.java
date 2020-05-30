package com.dnevi.expression.validator;

import com.dnevi.expression.validator.exception.InvalidExpressionException;
import com.dnevi.expression.validator.exception.UnknownExpressionTypeException;
import com.dnevi.expression.validator.expression.ExpressionValidator;
import com.dnevi.expression.validator.schema.StateSchema;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ValidatorApplicationTest {
    private StateSchema schema;
    private ExpressionValidator validator;

    @Before
    public void setup() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = this.getClass().getResourceAsStream("/example_state_schema.json");
        this.schema = mapper.readValue(inputStream, new TypeReference<>() {
        });

        this.validator = new ExpressionValidator();
    }

    @Test
    public void shouldValidateExpressions() {
        List<String> expressions = new ArrayList<>();
        expressions.add("($.age >= 30) || ($.age < 40)");
        expressions.add("$.name != null");
        expressions.add("$.name != \"null\"");
        expressions.add("$.name == \"someCoolName\"");
        expressions.add("$.position == 0");
        expressions.add("$.employed == false");
        expressions.add("(($.employed == false) && ($.employed != true)) ");
        expressions.add("$.employed != false && $.age > 60");
        expressions.add("((($.position != 4) || ($.position < 50)) != true)");
        expressions.add("($.age != 30) || ($.age < 40) && ($.employed == true)");

        expressions.forEach(e -> {
                    log.info("");
                    log.info("---------------------------------------------------");
                    log.info("Evaluating expression {}...", e);
                    boolean isValid = this.validator.validate(e, this.schema);
                    log.info("Expression is {}", isValid ? "valid." : "not valid.");
                    log.info("---------------------------------------------------");
                    Assert.assertTrue(isValid);
                }
        );
    }

    @Test
    public void shouldThrowInvalidExpressionException() {
        String expression = "($.age >= 30 || ($.age < 40)";

        Assert.assertThrows(InvalidExpressionException.class,
                () -> this.validator.validate(expression, this.schema));
    }

    @Test
    public void shouldThrowUnknownExpressionTypeException() {
        String expression = "($.name != John)";
        Assert.assertThrows(UnknownExpressionTypeException.class,
                () -> this.validator.validate(expression, this.schema));
    }
}
