package com.dnevi.expression.validator.expression;


import com.dnevi.expression.validator.exception.ParseExpressionException;
import com.dnevi.expression.validator.expression.Expression.Binary;
import com.dnevi.expression.validator.expression.Expression.Grouping;
import com.dnevi.expression.validator.expression.Expression.Literal;
import com.dnevi.expression.validator.expression.Expression.Logical;
import com.dnevi.expression.validator.validation.ValidationError;
import com.dnevi.expression.validator.validation.ValidatorContext;
import lombok.extern.slf4j.Slf4j;

/**
 * Class evaluates expression using the Visitor Pattern
 *
 * @see <a href="https://en.wikipedia.org/wiki/Visitor_pattern">The Visitor Pattern</a>
 */
@Slf4j
public class Interpreter implements Expression.Visitor<TokenType> {
    private ValidatorContext validatorContext;

    public Interpreter(ValidatorContext validatorContext) {
        this.validatorContext = validatorContext;
    }

    /**
     * Method will take in a syntax tree and recursively traversed it and computes values.
     *
     * @return {@link TokenType} enum values TRUE or FALSE represented as {@link Boolean} values
     */
    public boolean interpret(Expression expression) {
        try {
            TokenType value = this.evaluate(expression);

            return value.equals(TokenType.TRUE);
        } catch (ParseExpressionException e) {
            this.addError(String.format("Malformed expression. Interpreter could not evaluate %s.",
                    e.getLocalizedMessage()));
        }

        return false;
    }

    private TokenType evaluate(Expression expr) {
        return expr.accept(this);
    }

    @Override
    public TokenType visitLiteralExpr(Literal expr) {
        return expr.value;
    }

    @Override
    public TokenType visitLogicalExpr(Logical expr) {
        TokenType left = this.evaluate(expr.left);
        TokenType right = this.evaluate(expr.right);

        return (this.isBoolean(left, right) && this.isTruth(left, right)) ? TokenType.TRUE
                : TokenType.FALSE;
    }

    @Override
    public TokenType visitGroupingExpr(Grouping expr) {
        return this.evaluate(expr.expression);
    }

    @Override
    public TokenType visitBinaryExpr(Binary expr) {
        TokenType left = this.evaluate(expr.left);
        TokenType right = this.evaluate(expr.right);

        switch (expr.operator.getType()) {
            case GREATER:
            case GREATER_EQUAL:
            case LESS:
            case LESS_EQUAL:
                return (this.validateNumberOperands(expr.operator, left, right)) ? TokenType.TRUE
                        : TokenType.FALSE;
            case BANG_EQUAL:
            case EQUAL_EQUAL:
                return (this.validateAllOperands(expr.operator, left, right)) ? TokenType.TRUE
                        : TokenType.FALSE;
        }

        return null;
    }

    /**
     * The operators <, <=, > and >= can only be used for an INTEGER and NUMBER types. Method will
     * check and compare left and right expression as {@link TokenType}.
     *
     * @return statement result represented as {@link Boolean}
     */
    private boolean validateNumberOperands(Token operator,
            TokenType left, TokenType right) {
        if (TokenType.getAllOperators().contains(operator.getType())) {
            var isValidOperand = left.equals(TokenType.NUMBER) && right.equals(TokenType.NUMBER);
            if (!isValidOperand) {
                this.validatorContext.addError(new ValidationError(
                        "The operators <, <=, > and >= can only be used for an INTEGER and NUMBER types."));
            }

            return isValidOperand;
        }

        return false;
    }

    /**
     * Method will check and compare left and right expression as {@link TokenType}.
     *
     * @return statement result represented as {@link Boolean}
     */
    private boolean validateAllOperands(Token operator,
            TokenType left, TokenType right) {
        if (TokenType.getBooleanAndStringOperators().contains(operator.getType())) {
            if (isStringOrNull(left, right)) {
                return true;

            } else if (isBoolean(left, right)) {
                return true;
            } else return isNumber(left, right);
        }

        return false;
    }

    private boolean isStringOrNull(TokenType left, TokenType right) {
        var isStringOrNull = ((left.equals(TokenType.STRING) || left.equals(TokenType.NIL)) &&
                (right.equals(TokenType.STRING) || right.equals(TokenType.NIL)));
        if (!isStringOrNull) {
            this.validatorContext.addError(new ValidationError(
                    "The operators != and == can only be used for a string and null types."));
        }

        return isStringOrNull;
    }

    private boolean isBoolean(TokenType left, TokenType right) {
        var isBoolean = ((left.equals(TokenType.TRUE) || left.equals(TokenType.FALSE) || left
                .equals(TokenType.BOOLEAN)) &&
                (right.equals(TokenType.TRUE) || right.equals(TokenType.FALSE) || right
                        .equals(TokenType.BOOLEAN)));
        if (!isBoolean) {
            this.validatorContext.addError(new ValidationError("Not a valid boolean."));
        }

        return isBoolean;
    }

    private boolean isNumber(TokenType left, TokenType right) {
        var isNumber = left.equals(TokenType.NUMBER) && right.equals(TokenType.NUMBER);
        if (!isNumber) {
            this.validatorContext.addError(new ValidationError("Not a valid number."));
        }

        return isNumber;
    }

    private boolean isTruth(TokenType left, TokenType right) {
        if (left == null || right == null) {
            return false;
        }
        return !left.equals(TokenType.FALSE) && !right.equals(TokenType.FALSE);
    }

    private void addError(String message) {
        validatorContext.addError(
                new ValidationError(message));
    }
}
