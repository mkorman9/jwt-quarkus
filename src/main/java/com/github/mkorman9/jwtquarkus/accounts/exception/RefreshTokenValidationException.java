package com.github.mkorman9.jwtquarkus.accounts.exception;

public class RefreshTokenValidationException extends TokenValidationException {
    public RefreshTokenValidationException(Throwable cause) {
        super(cause);
    }
}
