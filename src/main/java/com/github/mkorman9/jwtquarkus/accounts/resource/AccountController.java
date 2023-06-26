package com.github.mkorman9.jwtquarkus.accounts.resource;

import com.github.mkorman9.jwtquarkus.accounts.dto.payload.AccountResponse;
import com.github.mkorman9.jwtquarkus.accounts.dto.payload.TokenRefreshPayload;
import com.github.mkorman9.jwtquarkus.accounts.dto.payload.TokenResponse;
import com.github.mkorman9.jwtquarkus.accounts.service.AccountService;
import com.github.mkorman9.jwtquarkus.accounts.service.RefreshTokenService;
import com.github.mkorman9.jwtquarkus.accounts.service.TokenGenerationService;
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
    TokenGenerationService tokenGenerationService;

    @Inject
    RefreshTokenService refreshTokenService;

    @GET
    @Path("/new")
    public AccountResponse newAccount() {
        var userId = accountService.registerAccount();

        var tokenPair = tokenGenerationService.generate(userId);
        var tokenResponse = TokenResponse.builder()
                .accessToken(tokenPair.getAccessToken().getToken())
                .refreshToken(tokenPair.getRefreshToken().getToken())
                .expiresAt(tokenPair.getAccessToken().getExpiresAt().toEpochMilli())
                .build();

        return AccountResponse.builder()
                .id(userId.toString())
                .token(tokenResponse)
                .build();
    }

    @PUT
    @Path("/token/refresh")
    public TokenResponse refreshToken(@NotNull TokenRefreshPayload payload) {
        var result = refreshTokenService.refresh(payload.getRefreshToken(), payload.getAccessToken());
        if (!result.isValid()) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        var tokenPair = tokenGenerationService.generate(result.getUserId());
        return TokenResponse.builder()
                .accessToken(tokenPair.getAccessToken().getToken())
                .refreshToken(tokenPair.getRefreshToken().getToken())
                .expiresAt(tokenPair.getAccessToken().getExpiresAt().toEpochMilli())
                .build();
    }
}
