package com.github.mkorman9.jwtquarkus.accounts.dto.payload;

import com.github.mkorman9.jwtquarkus.accounts.dto.TokenPair;
import lombok.Builder;

@Builder
public record TokenResponse(
    String accessToken,
    String refreshToken,
    long expiresAt
) {
    public static TokenResponse fromPair(TokenPair pair) {
        return TokenResponse.builder()
                .accessToken(pair.accessToken().token())
                .refreshToken(pair.refreshToken().token())
                .expiresAt(pair.accessToken().expiresAt().toEpochMilli())
                .build();
    }
}
