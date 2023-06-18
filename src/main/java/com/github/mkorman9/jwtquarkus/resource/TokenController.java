package com.github.mkorman9.jwtquarkus.resource;

import com.github.mkorman9.jwtquarkus.dto.TokenResponse;
import com.github.mkorman9.jwtquarkus.service.AccessTokenService;
import com.github.mkorman9.jwtquarkus.service.RefreshTokenService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/token")
public class TokenController {
    @Inject
    AccessTokenService accessTokenService;

    @Inject
    RefreshTokenService refreshTokenService;

    @GET
    public TokenResponse getToken() {
        var accessToken = accessTokenService.generate("admin");
        var refreshToken = refreshTokenService.generate(accessToken);

        return TokenResponse.builder()
                .accessToken(accessToken.getToken())
                .refreshToken(refreshToken.getToken())
                .expiresAt(accessToken.getExpiresAt().toEpochMilli())
                .build();
    }
}
