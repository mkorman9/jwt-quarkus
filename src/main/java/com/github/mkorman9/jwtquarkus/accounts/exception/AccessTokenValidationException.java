package com.github.mkorman9.jwtquarkus.accounts.exception;

public class AccessTokenValidationException extends RuntimeException {
    public AccessTokenValidationException(Throwable cause) {
        super(cause);
    }
}
