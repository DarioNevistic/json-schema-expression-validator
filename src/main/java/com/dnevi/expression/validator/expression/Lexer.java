package com.dnevi.expression.validator.expression;

import com.dnevi.expression.validator.exception.InvalidExpressionException;
import com.dnevi.expression.validator.exception.UnknownExpressionTypeException;
import com.dnevi.expression.validator.validation.ValidationError;
import com.dnevi.expression.validator.validation.ValidatorContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Class performs the Lexical Analysis of expression
 */
public class Lexer {
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;

    private final String expression;
    private ValidatorContext validatorContext;

    public Lexer(String expression,
            ValidatorContext validatorContext) {
        if (!this.isParenthesisMatch(expression)) {
            throw new InvalidExpressionException(expression);
        }
        this.expression = expression;
        this.validatorContext = validatorContext;
    }

    public List<Token> scanTokens() {
        while (!this.isAtEnd()) {
            start = this.getCurrent();
            this.scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null));
        return tokens;
    }

    private void scanToken() {
        char c = this.advance();
        switch (c) {
            case '(':
                this.addToken(TokenType.LEFT_PAREN);
                break;
            case ')':
                this.addToken(TokenType.RIGHT_PAREN);
                break;
            case '!':
                this.addToken(this.match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                break;
            case '=':
                this.addToken(this.match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                break;
            case '<':
                this.addToken(this.match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;
            case '>':
                this.addToken(this.match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;
            case '|':
                if (this.match('|')) {
                    this.addToken(TokenType.OR);
                }
                break;
            case '&':
                if (this.match('&')) {
                    this.addToken(TokenType.AND);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace.
                break;
            case '$':
                // Find JSON_PATH.
                if (this.match('.')) {
                    this.jsonPath();
                }
                break;
            case '"':
                this.string();
                break;
            default:
                if (this.isDigit(c)) {
                    this.number();
                } else if (this.isAlpha(c)) {
                    this.identifier();
                } else {
                    this.addError(String.format("Unexpected character '%s' in expression %s",
                            c, expression));
                }
        }
    }

    private void addToken(TokenType type) {
        this.addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = expression.substring(start, this.getCurrent());
        tokens.add(new Token(type, text, literal));
    }

    private boolean match(char expected) {
        if (this.isAtEnd()) {
            return false;
        }

        if (expression.charAt(this.getCurrent()) != expected) {
            return false;
        }

        this.incrementCurrent();
        return true;
    }

    private char advance() {
        this.incrementCurrent();
        return expression.charAt(this.getCurrent() - 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z');
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void string() {
        while (this.peek() != '"' && !this.isAtEnd()) {
            this.advance();
        }

        if (this.isAtEnd()) {
            this.addError("Unterminated string.");
            return;
        }

        this.advance();

        String value = expression.substring(start + 1, this.getCurrent() - 1);
        this.addToken(TokenType.STRING, value);
    }

    private void identifier() {
        String identifier;
        StringBuilder sb = new StringBuilder();
        sb.append(expression.charAt(this.getCurrent() - 1));
        while (this.isAlphaNumeric(this.peek())) {
            sb.append(this.advance());
        }
        identifier = sb.toString();

        switch (identifier.toLowerCase()) {
            case "false":
                this.addToken(TokenType.FALSE);
                break;
            case "true":
                this.addToken(TokenType.TRUE);
                break;
            case "null":
                this.addToken(TokenType.NIL);
                break;
            default:
                throw new UnknownExpressionTypeException(identifier, expression);

        }
    }

    private boolean isAlphaNumeric(char c) {
        return this.isAlpha(c) || this.isDigit(c);
    }

    private void number() {
        while (this.isDigit(this.peek())) {
            this.advance();
        }

        if (this.peek() == '.' && this.isDigit(this.peekNext())) {
            this.advance();

            while (this.isDigit(this.peek())) { this.advance(); }
        }

        addToken(TokenType.NUMBER);
    }

    private void jsonPath() {
        while (this.isAlpha(this.peek())) {
            this.advance();
        }

        this.addToken(TokenType.JSON_PATH);
    }

    private char peek() {
        if (this.isAtEnd()) {
            return '\0';
        }

        return expression.charAt(this.getCurrent());
    }

    private char peekNext() {
        if (this.getCurrent() + 1 >= expression.length()) {
            return '\0';
        }

        return expression.charAt(this.getCurrent() + 1);
    }

    private void incrementCurrent() {
        current++;
    }

    private int getCurrent() {
        return this.current;
    }

    private boolean isAtEnd() {
        return this.getCurrent() >= expression.length();
    }

    private void addError(String message) {
        validatorContext.addError(
                new ValidationError(message));
    }

    private boolean isParenthesisMatch(String expression) {
        Stack<Character> stack = new Stack<>();
        for (int i = 0; i < expression.length(); i++) {
            char current = expression.charAt(i);
            if (current == '(') {
                stack.push(current);
            }

            if (current == ')') {
                if (stack.isEmpty()) {
                    return false;
                }

                char last = stack.peek();
                if (last == '(') {
                    stack.pop();
                } else {
                    return false;
                }
            }

        }

        return stack.isEmpty();
    }
}