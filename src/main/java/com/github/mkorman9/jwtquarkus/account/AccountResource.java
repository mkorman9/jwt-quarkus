package com.github.mkorman9.jwtquarkus.account;

import com.github.mkorman9.jwtquarkus.token.TokenFacade;
import com.github.mkorman9.jwtquarkus.token.TokenPair;
import com.github.mkorman9.jwtquarkus.token.TokenResponse;
import com.github.mkorman9.jwtquarkus.token.exception.TokenRefreshException;
import com.github.mkorman9.jwtquarkus.token.exception.TokenValidationException;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Builder;

@Path("/account")
@Produces(MediaType.APPLICATION_JSON)
public class AccountResource {
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
    public TokenResponse refreshToken(@NotNull @Valid TokenRefreshPayload payload) {
        TokenPair tokenPair;
        try {
            tokenPair = tokenFacade.refreshToken(payload.accessToken(), payload.refreshToken());
        } catch (TokenValidationException | TokenRefreshException e) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        return TokenResponse.fromPair(tokenPair);
    }

    public record TokenRefreshPayload(
        @NotBlank String accessToken,
        @NotBlank String refreshToken
    ) {
    }

    @Builder
    public record AccountResponse(
        String id,
        TokenResponse token
    ) {
    }
}
