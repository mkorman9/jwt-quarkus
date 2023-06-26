package com.github.mkorman9.jwtquarkus.accounts.dto.payload;

import com.github.mkorman9.jwtquarkus.accounts.dto.TokenPair;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {
    private String accessToken;

    private String refreshToken;

    private long expiresAt;

    public static TokenResponse fromPair(TokenPair pair) {
        return TokenResponse.builder()
                .accessToken(pair.getAccessToken().getToken())
                .refreshToken(pair.getRefreshToken().getToken())
                .expiresAt(pair.getAccessToken().getExpiresAt().toEpochMilli())
                .build();
    }
}
