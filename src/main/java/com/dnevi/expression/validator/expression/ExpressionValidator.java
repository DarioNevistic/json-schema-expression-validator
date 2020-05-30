package com.dnevi.expression.validator.expression;

import com.dnevi.expression.validator.schema.StateSchema;
import com.dnevi.expression.validator.validation.ValidationBubble;
import com.dnevi.expression.validator.validation.ValidationError;
import com.dnevi.expression.validator.validation.ValidationResult;
import com.dnevi.expression.validator.validation.ValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ExpressionValidator implements ValidationBubble {
    private ValidationResult validationResult;

    /**
     * Method will scan expression and convert each character to list of {@link Token}, parse
     * scanned tokens using recursive descent parser and at the end evaluate expression in
     * Interpreter
     *
     * @param expression A valid Json Schema expression
     * @param stateSchema Current input state schema
     * @return Result of interpreted expression represented as a boolean value
     */
    public boolean validate(String expression, StateSchema stateSchema) {
        var validatorContext = new ValidatorContext();
        if (expression == null || expression.isEmpty()) {
            validatorContext.addError(new ValidationError("Malformed expression %s."));
        }

        Lexer lexer = new Lexer(expression, validatorContext);
        List<Token> tokens = lexer.scanTokens();

        Parser parser = new Parser(tokens, stateSchema, validatorContext);
        Expression expr = parser.parse();
        if (expr == null) {
            validatorContext.addError(new ValidationError("Malformed expression %s."));
        }

        Interpreter interpreter = new Interpreter(validatorContext);
        var isValid = interpreter.interpret(expr);

        var results = validatorContext.getValidationResult();
        if (results.hasErrors()) {
            this.validationResult = results;
        }

        return isValid;
    }

    @Override
    public boolean isInvalid() {
        return this.validationResult != null && this.getValidationResult().hasErrors();
    }

    @Override
    public ValidationResult getValidationResult() {
        return this.validationResult;
    }

}
