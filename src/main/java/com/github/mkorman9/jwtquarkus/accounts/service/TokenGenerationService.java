package com.github.mkorman9.jwtquarkus.accounts.service;

import com.github.mkorman9.jwtquarkus.accounts.dto.TokenPair;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class TokenGenerationService {
    @Inject
    AccessTokenService accessTokenService;

    @Inject
    RefreshTokenService refreshTokenService;

    public TokenPair generate(UUID userId) {
        var accessToken = accessTokenService.generate(userId);
        var refreshToken = refreshTokenService.generate(accessToken);

        return TokenPair.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
