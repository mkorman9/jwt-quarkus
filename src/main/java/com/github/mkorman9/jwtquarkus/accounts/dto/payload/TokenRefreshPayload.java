package com.github.mkorman9.jwtquarkus.accounts.dto.payload;

public record TokenRefreshPayload(
    String accessToken,
    String refreshToken
) {
}
