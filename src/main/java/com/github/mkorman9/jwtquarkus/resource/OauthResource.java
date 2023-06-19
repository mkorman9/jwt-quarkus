package com.github.mkorman9.jwtquarkus.resource;

import com.github.mkorman9.jwtquarkus.dto.AccessToken;
import com.github.mkorman9.jwtquarkus.dto.OauthAuthorization;
import com.github.mkorman9.jwtquarkus.dto.TokenResponse;
import com.github.mkorman9.jwtquarkus.exception.AccessTokenValidationException;
import com.github.mkorman9.jwtquarkus.exception.GithubAccountAlreadyUsedException;
import com.github.mkorman9.jwtquarkus.exception.GithubAccountNotFoundException;
import com.github.mkorman9.jwtquarkus.exception.OauthFlowException;
import com.github.mkorman9.jwtquarkus.exception.OauthStateValidationException;
import com.github.mkorman9.jwtquarkus.service.GithubOauthService;
import com.github.mkorman9.jwtquarkus.service.RefreshTokenService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestCookie;
import org.jboss.resteasy.reactive.RestQuery;
import org.jboss.resteasy.reactive.RestResponse;

import java.net.URI;
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

    @Inject
    RefreshTokenService refreshTokenService;

    @GET
    @Path("/login")
    public RestResponse<Object> login() {
        var auth = githubOauthService.beginLogin();

        return RestResponse.ResponseBuilder
                .seeOther(URI.create(auth.getUrl()))
                .cookie(
                        new NewCookie.Builder(OAUTH2_COOKIE)
                                .value(auth.getState().getCookie())
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
    public RestResponse<Object> connectAccount(
            @RestQuery("accessToken") Optional<String> accessToken
    ) {
        if (accessToken.isEmpty()) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        OauthAuthorization auth;
        try {
            auth = githubOauthService.beginConnectAccount(accessToken.get());
        } catch (AccessTokenValidationException e) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        return RestResponse.ResponseBuilder
                .seeOther(URI.create(auth.getUrl()))
                .cookie(
                        new NewCookie.Builder(OAUTH2_COOKIE)
                                .value(auth.getState().getCookie())
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
    public RestResponse<TokenResponse> loginCallback(
            @RestQuery Optional<String> code,
            @RestQuery Optional<String> state,
            @RestCookie(OAUTH2_COOKIE) Optional<String> cookie
    ) {
        if (code.isEmpty() || state.isEmpty() || cookie.isEmpty()) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        AccessToken token;
        try {
            token = githubOauthService.finishLogin(code.get(), state.get(), cookie.get());
        } catch (OauthStateValidationException | OauthFlowException | GithubAccountNotFoundException e) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        var refreshToken = refreshTokenService.generate(token);
        var response = TokenResponse.builder()
                .accessToken(token.getToken())
                .refreshToken(refreshToken.getToken())
                .expiresAt(token.getExpiresAt().toEpochMilli())
                .build();

        return RestResponse.ResponseBuilder
                .ok(response)
                .cookie(
                        new NewCookie.Builder(ACCESS_TOKEN_COOKIE)
                                .value(token.getToken())
                                .sameSite(NewCookie.SameSite.STRICT)
                                .httpOnly(true)
                                .build(),
                        new NewCookie.Builder(REFRESH_TOKEN_COOKIE)
                                .value(refreshToken.getToken())
                                .sameSite(NewCookie.SameSite.STRICT)
                                .httpOnly(true)
                                .build(),
                        new NewCookie.Builder(EXPIRES_AT_COOKIE)
                                .value(Long.toString(token.getExpiresAt().toEpochMilli()))
                                .sameSite(NewCookie.SameSite.STRICT)
                                .httpOnly(true)
                                .build()
                )
                .build();
    }

    @GET
    @Path("/callback/connect-account")
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
