package com.github.mkorman9.jwtquarkus.accounts.exception;

public class TokenRefreshException extends RuntimeException {
    public TokenRefreshException(String message) {
        super(message);
    }
}
