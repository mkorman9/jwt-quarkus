package com.github.mkorman9.jwtquarkus.oauth.resource;

import com.github.mkorman9.jwtquarkus.accounts.dto.TokenPair;
import com.github.mkorman9.jwtquarkus.accounts.dto.payload.TokenResponse;
import com.github.mkorman9.jwtquarkus.accounts.exception.AccessTokenValidationException;
import com.github.mkorman9.jwtquarkus.oauth.dto.OauthTicket;
import com.github.mkorman9.jwtquarkus.oauth.exception.GithubAccountAlreadyUsedException;
import com.github.mkorman9.jwtquarkus.oauth.exception.GithubAccountNotFoundException;
import com.github.mkorman9.jwtquarkus.oauth.exception.OauthFlowException;
import com.github.mkorman9.jwtquarkus.oauth.exception.OauthStateValidationException;
import com.github.mkorman9.jwtquarkus.oauth.service.GithubOauthService;
import io.quarkiverse.bucket4j.runtime.RateLimited;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestCookie;
import org.jboss.resteasy.reactive.RestQuery;
import org.jboss.resteasy.reactive.RestResponse;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Path("/oauth")
public class OauthResource {
    private static final String OAUTH2_COOKIE = "oauth2_cookie";
    private static final String ACCESS_TOKEN_COOKIE = "access_token";
    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";
    private static final String EXPIRES_AT_COOKIE = "expires_at";

    @Inject
    GithubOauthService githubOauthService;

    @GET
    @Path("/login")
    @RateLimited(bucket = "oauth")
    public RestResponse<Object> login() {
        var ticket = githubOauthService.beginLogin();

        return RestResponse.ResponseBuilder
                .seeOther(ticket.getUrl())
                .cookie(
                        new NewCookie.Builder(OAUTH2_COOKIE)
                                .value(ticket.getState().getCookie())
                                .expiry(Date.from(
                                        Instant.now().plus(Duration.ofMinutes(5))
                                ))
                                .sameSite(NewCookie.SameSite.STRICT)
                                .httpOnly(true)
                                .build()
                )
                .build();
    }

    @GET
    @Path("/connect-account")
    @RateLimited(bucket = "oauth")
    public RestResponse<Object> connectAccount(
            @RestQuery("accessToken") Optional<String> accessToken
    ) {
        if (accessToken.isEmpty()) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        OauthTicket ticket;
        try {
            ticket = githubOauthService.beginConnectAccount(accessToken.get());
        } catch (AccessTokenValidationException e) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        return RestResponse.ResponseBuilder
                .seeOther(ticket.getUrl())
                .cookie(
                        new NewCookie.Builder(OAUTH2_COOKIE)
                                .value(ticket.getState().getCookie())
                                .expiry(Date.from(
                                        Instant.now().plus(Duration.ofMinutes(5))
                                ))
                                .sameSite(NewCookie.SameSite.STRICT)
                                .httpOnly(true)
                                .build()
                )
                .build();
    }

    @GET
    @Path("/callback/login")
    @RateLimited(bucket = "oauth")
    public RestResponse<TokenResponse> loginCallback(
            @RestQuery Optional<String> code,
            @RestQuery Optional<String> state,
            @RestCookie(OAUTH2_COOKIE) Optional<String> cookie
    ) {
        if (code.isEmpty() || state.isEmpty() || cookie.isEmpty()) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        TokenPair tokenPair;
        try {
            tokenPair = githubOauthService.finishLogin(code.get(), state.get(), cookie.get());
        } catch (OauthStateValidationException | OauthFlowException | GithubAccountNotFoundException e) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        var response = TokenResponse.builder()
                .accessToken(tokenPair.getAccessToken().getToken())
                .refreshToken(tokenPair.getRefreshToken().getToken())
                .expiresAt(tokenPair.getAccessToken().getExpiresAt().toEpochMilli())
                .build();

        return RestResponse.ResponseBuilder
                .ok(response)
                .cookie(
                        new NewCookie.Builder(ACCESS_TOKEN_COOKIE)
                                .value(tokenPair.getAccessToken().getToken())
                                .sameSite(NewCookie.SameSite.STRICT)
                                .httpOnly(true)
                                .build(),
                        new NewCookie.Builder(REFRESH_TOKEN_COOKIE)
                                .value(tokenPair.getRefreshToken().getToken())
                                .sameSite(NewCookie.SameSite.STRICT)
                                .httpOnly(true)
                                .build(),
                        new NewCookie.Builder(EXPIRES_AT_COOKIE)
                                .value(Long.toString(tokenPair.getAccessToken().getExpiresAt().toEpochMilli()))
                                .sameSite(NewCookie.SameSite.STRICT)
                                .httpOnly(true)
                                .build()
                )
                .build();
    }

    @GET
    @Path("/callback/connect-account")
    @RateLimited(bucket = "oauth")
    public String connectAccountCallback(
            @RestQuery Optional<String> code,
            @RestQuery Optional<String> state,
            @RestCookie(OAUTH2_COOKIE) Optional<String> cookie
    ) {
        if (code.isEmpty() || state.isEmpty() || cookie.isEmpty()) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        try {
            githubOauthService.finishConnectAccount(code.get(), state.get(), cookie.get());
        } catch (OauthStateValidationException | OauthFlowException e) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } catch (GithubAccountAlreadyUsedException e) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        return "OK";
    }
}
