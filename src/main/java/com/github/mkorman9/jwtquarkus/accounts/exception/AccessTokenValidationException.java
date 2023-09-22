package com.github.mkorman9.jwtquarkus.accounts.exception;

public class AccessTokenValidationException extends TokenValidationException {
    public AccessTokenValidationException(Throwable cause) {
        super(cause);
    }
}
