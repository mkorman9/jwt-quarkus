package com.github.mkorman9.jwtquarkus.token.exception;

public class RefreshTokenValidationException extends TokenValidationException {
    public RefreshTokenValidationException(Throwable cause) {
        super(cause);
    }
}
