package com.github.mkorman9.jwtquarkus.accounts.resource;

import com.github.mkorman9.jwtquarkus.accounts.dto.TokenPair;
import com.github.mkorman9.jwtquarkus.accounts.dto.payload.AccountResponse;
import com.github.mkorman9.jwtquarkus.accounts.dto.payload.TokenRefreshPayload;
import com.github.mkorman9.jwtquarkus.accounts.dto.payload.TokenResponse;
import com.github.mkorman9.jwtquarkus.accounts.exception.AccessTokenValidationException;
import com.github.mkorman9.jwtquarkus.accounts.exception.RefreshTokenValidationException;
import com.github.mkorman9.jwtquarkus.accounts.exception.TokenRefreshException;
import com.github.mkorman9.jwtquarkus.accounts.service.AccountService;
import com.github.mkorman9.jwtquarkus.accounts.service.TokenFacade;
import io.smallrye.common.constraint.NotNull;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/account")
@Produces(MediaType.APPLICATION_JSON)
public class AccountController {
    @Inject
    AccountService accountService;

    @Inject
    TokenFacade tokenFacade;

    @GET
    @Path("/new")
    public AccountResponse newAccount() {
        var userId = accountService.registerAccount();
        var tokenPair = tokenFacade.generatePair(userId);

        return AccountResponse.builder()
            .id(userId.toString())
            .token(TokenResponse.fromPair(tokenPair))
            .build();
    }

    @PUT
    @Path("/token/refresh")
    @Consumes(MediaType.APPLICATION_JSON)
    public TokenResponse refreshToken(@NotNull TokenRefreshPayload payload) {
        TokenPair tokenPair;
        try {
            tokenPair = tokenFacade.refreshToken(payload.accessToken(), payload.refreshToken());
        } catch (AccessTokenValidationException | RefreshTokenValidationException | TokenRefreshException e) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        return TokenResponse.fromPair(tokenPair);
    }
}
