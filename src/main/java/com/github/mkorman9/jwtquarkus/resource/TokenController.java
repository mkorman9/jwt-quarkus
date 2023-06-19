package com.github.mkorman9.jwtquarkus.resource;

import com.github.mkorman9.jwtquarkus.dto.TokenRefreshPayload;
import com.github.mkorman9.jwtquarkus.dto.TokenResponse;
import com.github.mkorman9.jwtquarkus.service.AccessTokenService;
import com.github.mkorman9.jwtquarkus.service.RefreshTokenService;
import io.smallrye.common.constraint.NotNull;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@Path("/token")
public class TokenController {
    @Inject
    AccessTokenService accessTokenService;

    @Inject
    RefreshTokenService refreshTokenService;

    @GET
    public TokenResponse getToken() {
        var accessToken = accessTokenService.generate("9effb409-532e-4586-bdbe-41611d41e482");
        var refreshToken = refreshTokenService.generate(accessToken);

        return TokenResponse.builder()
                .accessToken(accessToken.getToken())
                .refreshToken(refreshToken.getToken())
                .expiresAt(accessToken.getExpiresAt().toEpochMilli())
                .build();
    }

    @PUT
    @Path("/refresh")
    public TokenResponse refreshToken(@NotNull TokenRefreshPayload payload) {
        var validation = refreshTokenService.validateRefreshToken(payload.getRefreshToken(), payload.getAccessToken());
        if (!validation.isValid()) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        var accessToken = accessTokenService.generate(validation.getUserId().toString());
        var refreshToken = refreshTokenService.generate(accessToken);

        return TokenResponse.builder()
                .accessToken(accessToken.getToken())
                .refreshToken(refreshToken.getToken())
                .expiresAt(accessToken.getExpiresAt().toEpochMilli())
                .build();
    }
}
