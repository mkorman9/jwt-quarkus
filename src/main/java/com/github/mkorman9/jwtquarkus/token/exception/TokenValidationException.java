package com.github.mkorman9.jwtquarkus.token.exception;

public abstract class TokenValidationException extends RuntimeException {
    public TokenValidationException(Throwable cause) {
        super(cause);
    }
}
