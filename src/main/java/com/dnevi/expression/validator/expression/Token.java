package com.dnevi.expression.validator.expression;

import lombok.Getter;

@Getter
public class Token {
    private TokenType type;
    private final String lexeme;
    private final Object literal;

    Token(TokenType type, String lexeme, Object literal) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
    }

    public void setType(TokenType type) {
        this.type = type;
    }

    public String toString() {
        return type + " " + lexeme;
    }
}
