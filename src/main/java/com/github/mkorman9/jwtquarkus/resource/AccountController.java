package com.github.mkorman9.jwtquarkus.resource;

import com.github.mkorman9.jwtquarkus.dto.TokenRefreshPayload;
import com.github.mkorman9.jwtquarkus.dto.TokenResponse;
import com.github.mkorman9.jwtquarkus.service.AccessTokenService;
import com.github.mkorman9.jwtquarkus.service.AccountService;
import com.github.mkorman9.jwtquarkus.service.RefreshTokenService;
import io.smallrye.common.constraint.NotNull;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@Path("/account")
public class AccountController {
    @Inject
    AccountService accountService;

    @Inject
    AccessTokenService accessTokenService;

    @Inject
    RefreshTokenService refreshTokenService;

    @GET
    @Path("/new")
    public TokenResponse newAccount() {
        var userId = accountService.registerAccount();

        var accessToken = accessTokenService.generate(userId);
        var refreshToken = refreshTokenService.generate(accessToken);

        return TokenResponse.builder()
                .accessToken(accessToken.getToken())
                .refreshToken(refreshToken.getToken())
                .expiresAt(accessToken.getExpiresAt().toEpochMilli())
                .build();
    }

    @PUT
    @Path("/token/refresh")
    public TokenResponse refreshToken(@NotNull TokenRefreshPayload payload) {
        var result = refreshTokenService.refresh(payload.getRefreshToken(), payload.getAccessToken());
        if (!result.isValid()) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        var accessToken = accessTokenService.generate(result.getUserId());
        var refreshToken = refreshTokenService.generate(accessToken);

        return TokenResponse.builder()
                .accessToken(accessToken.getToken())
                .refreshToken(refreshToken.getToken())
                .expiresAt(accessToken.getExpiresAt().toEpochMilli())
                .build();
    }
}