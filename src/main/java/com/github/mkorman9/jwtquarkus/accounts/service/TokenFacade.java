package com.github.mkorman9.jwtquarkus.accounts.service;

import com.github.mkorman9.jwtquarkus.accounts.dto.TokenPair;
import com.github.mkorman9.jwtquarkus.accounts.exception.TokenRefreshException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class TokenFacade {
    @Inject
    AccessTokenService accessTokenService;

    @Inject
    RefreshTokenService refreshTokenService;

    public TokenPair generatePair(UUID userId) {
        var accessToken = accessTokenService.generate(userId);
        var refreshToken = refreshTokenService.generate(accessToken);

        return TokenPair.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public TokenPair refreshToken(String accessToken, String refreshToken) {
        var result = refreshTokenService.refresh(refreshToken, accessToken);
        if (!result.isValid()) {
            throw new TokenRefreshException();
        }

        return generatePair(result.getUserId());
    }
}
