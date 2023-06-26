package com.github.mkorman9.jwtquarkus.accounts.service;

import com.github.mkorman9.jwtquarkus.accounts.dto.TokenPair;
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
        var userId = refreshTokenService.refresh(accessToken, refreshToken);
        return generatePair(userId);
    }
}
