package com.github.mkorman9.jwtquarkus.accounts.exception;

public class RefreshTokenValidationException extends RuntimeException {
    public RefreshTokenValidationException(Throwable cause) {
        super(cause);
    }
}
