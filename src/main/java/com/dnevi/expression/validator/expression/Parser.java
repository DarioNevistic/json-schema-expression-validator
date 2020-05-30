package com.dnevi.expression.validator.expression;


import com.dnevi.expression.validator.exception.InvalidStatePathTypeException;
import com.dnevi.expression.validator.schema.Schema;
import com.dnevi.expression.validator.schema.StatePath;
import com.dnevi.expression.validator.schema.StateSchema;
import com.dnevi.expression.validator.validation.ValidationError;
import com.dnevi.expression.validator.validation.ValidatorContext;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Class parses series of tokens - we map those tokens to terminals in the grammar to figure out
 * which rules could have generated that string.
 *
 * Grammar we used:
 * <pre>
 * {@code
 * expression     → equality ;
 * equality       → comparison ( ( "!=" | "==" ) comparison )* ;
 * comparison     → primary ( ( ">" | ">=" | "<" | "<=" ) primary )* ;
 * primary        → NUMBER | STRING | BOOLEAN | "nil"
 *                | "(" expression ")" ;
 * }
 * </pre>
 */
public class Parser {
    private List<Token> tokens;
    private int current = 0;
    private ValidatorContext validatorContext;

    public Parser(List<Token> tokens, StateSchema stateSchema,
            ValidatorContext validatorContext) {
        this.validatorContext = validatorContext;
        this.tokens = this.mapStatePathTokens(tokens, stateSchema);
    }

    public Expression parse() {
        return this.expression();
    }

    private Expression expression() {
        return this.or();
    }

    private Expression or() {
        Expression expr = this.and();

        while (this.match(TokenType.OR)) {
            Token operator = this.previous();
            Expression right = this.and();
            expr = new Expression.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expression and() {
        Expression expr = this.equality();

        while (this.match(TokenType.AND)) {
            Token operator = this.previous();
            Expression right = this.equality();
            expr = new Expression.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expression equality() {
        Expression expr = this.comparison();

        while (this.match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = this.previous();
            Expression right = this.comparison();
            expr = new Expression.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expression comparison() {
        Expression expr = this.primary();

        while (this.match(
                TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = this.previous();
            Expression right = this.primary();
            expr = new Expression.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expression primary() {
        if (this.match(TokenType.TRUE)) {
            return new Expression.Literal(TokenType.TRUE);
        }
        if (this.match(TokenType.FALSE)) {
            return new Expression.Literal(TokenType.FALSE);
        }
        if (this.match(TokenType.BOOLEAN)) {
            return new Expression.Literal(TokenType.BOOLEAN);
        }
        if (this.match(TokenType.NIL)) {
            return new Expression.Literal(TokenType.NIL);
        }
        if (this.match(TokenType.JSON_PATH)) {
            return new Expression.Literal(previous().getType());
        }
        if (this.match(TokenType.NUMBER, TokenType.STRING)) {
            return new Expression.Literal(previous().getType());
        }
        if (this.match(TokenType.LEFT_PAREN)) {
            Expression expr = this.expression();
            this.consume();
            return new Expression.Grouping(expr);
        }

        this.addError("Expect expression.");

        return null;
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (this.checkTokenType(type)) {
                this.advance();
                return true;
            }
        }

        return false;
    }

    private void consume() {
        if (this.checkTokenType(TokenType.RIGHT_PAREN)) {
            this.advance();
            return;
        }

        this.addError("Expect ')' after expression.");
    }

    private boolean checkTokenType(TokenType type) {
        if (this.isAtEnd()) return false;
        return this.peek().getType() == type;
    }

    private void advance() {
        if (!this.isAtEnd()) current++;
    }

    private boolean isAtEnd() {
        return this.peek().getType() == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    /**
     * Since interpreter doesn't recognize JSON_PATH  as some data type (STRING, NUMBER, BOOLEAN),
     * method will find each JSON_PATH in tokens list and replace it with actual JSON_PATH type from
     * current State Schema. {@link StateSchema}. i.e.
     * <pre>
     * {@code
     * ((JSON_PATH) > NUMBER) && (JSON_PATH <= NUMBER)) - Interpreter does not know how to interpret
     * ((NUMBER) > NUMBER) && (NUMBER <= NUMBER)) - Interpreter knows how to interpret
     * }
     * </pre>
     */
    private List<Token> mapStatePathTokens(List<Token> tokens, StateSchema stateSchema) {
        return tokens.stream()
                .map(t -> {
                    if (!t.getType().equals(TokenType.JSON_PATH)) {
                        return t;
                    }
                    return this.replaceStatePathWithTokenType(t, stateSchema);
                })
                .collect(Collectors.toList());
    }

    private Token replaceStatePathWithTokenType(Token token, StateSchema stateSchema) {
        Optional<Schema> optionalSchema = this
                .getStateSchema(new StatePath(token.getLexeme()), stateSchema);

        if (optionalSchema.isEmpty()) {
            this.addError(String.format("Path '%s' is not valid.", token.getLexeme()));
            return token;
        }

        TokenType type = TokenType.valueOf(optionalSchema.get().getType());
        switch (type) {
            case INTEGER:
            case NUMBER:
                return new Token(TokenType.NUMBER, token.getLexeme(), token.getLiteral());
            case STRING:
                return new Token(TokenType.STRING, token.getLexeme(), token.getLiteral());
            case TRUE:
                return new Token(TokenType.TRUE, token.getLexeme(), token.getLiteral());
            case FALSE:
                return new Token(TokenType.FALSE, token.getLexeme(), token.getLiteral());
            case BOOLEAN:
                return new Token(TokenType.BOOLEAN, token.getLexeme(), token.getLiteral());
            default:
                throw new InvalidStatePathTypeException(type.toString());
        }
    }

    private void addError(String message) {
        validatorContext.addError(
                new ValidationError(message));
    }

    /**
     * @param statePath JSON_PATH from expression {@link StatePath}
     * @param stateSchema Current input state schema {@link StateSchema}
     * @return {@link Schema} if statePath exists in current State
     */
    private Optional<Schema> getStateSchema(StatePath statePath, StateSchema stateSchema) {
        return stateSchema.findSchemaByStatePath(statePath);
    }
}