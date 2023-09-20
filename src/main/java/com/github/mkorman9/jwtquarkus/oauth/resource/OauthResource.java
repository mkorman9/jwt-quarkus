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
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.RestCookie;
import org.jboss.resteasy.reactive.RestQuery;
import org.jboss.resteasy.reactive.RestResponse;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Path("/oauth")
@Slf4j
@Produces(MediaType.APPLICATION_JSON)
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
            .seeOther(ticket.url())
            .cookie(
                // cookie has to be LAX instead of STRICT because of the firefox bug
                // https://bugzilla.mozilla.org/show_bug.cgi?id=1465402
                new NewCookie.Builder(OAUTH2_COOKIE)
                    .value(ticket.state().cookie())
                    .expiry(Date.from(
                        Instant.now().plus(Duration.ofMinutes(5))
                    ))
                    .sameSite(NewCookie.SameSite.LAX)
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
            log.error("Missing access token");
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        OauthTicket ticket;
        try {
            ticket = githubOauthService.beginConnectAccount(accessToken.get());
        } catch (AccessTokenValidationException e) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        return RestResponse.ResponseBuilder
            .seeOther(ticket.url())
            .cookie(
                // cookie has to be LAX instead of STRICT because of the firefox bug
                // https://bugzilla.mozilla.org/show_bug.cgi?id=1465402
                new NewCookie.Builder(OAUTH2_COOKIE)
                    .value(ticket.state().cookie())
                    .expiry(Date.from(
                        Instant.now().plus(Duration.ofMinutes(5))
                    ))
                    .sameSite(NewCookie.SameSite.LAX)
                    .httpOnly(true)
                    .build()
            )
            .build();
    }

    @GET
    @Path("/callback/login")
    @RateLimited(bucket = "oauth")
    @RunOnVirtualThread
    public RestResponse<TokenResponse> loginCallback(
        @RestQuery Optional<String> code,
        @RestQuery Optional<String> state,
        @RestCookie(OAUTH2_COOKIE) Optional<String> cookie
    ) {
        if (code.isEmpty() || state.isEmpty() || cookie.isEmpty()) {
            log.error("Missing request params");
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        TokenPair tokenPair;
        try {
            tokenPair = githubOauthService.finishLogin(code.get(), state.get(), cookie.get());
        } catch (OauthStateValidationException | OauthFlowException | GithubAccountNotFoundException e) {
            log.error("OAuth2 exception");
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        return RestResponse.ResponseBuilder
            .ok(TokenResponse.fromPair(tokenPair))
            .cookie(
                new NewCookie.Builder(ACCESS_TOKEN_COOKIE)
                    .value(tokenPair.accessToken().token())
                    .sameSite(NewCookie.SameSite.STRICT)
                    .httpOnly(true)
                    .build(),
                new NewCookie.Builder(REFRESH_TOKEN_COOKIE)
                    .value(tokenPair.refreshToken().token())
                    .sameSite(NewCookie.SameSite.STRICT)
                    .httpOnly(true)
                    .build(),
                new NewCookie.Builder(EXPIRES_AT_COOKIE)
                    .value(Long.toString(tokenPair.accessToken().expiresAt().toEpochMilli()))
                    .sameSite(NewCookie.SameSite.STRICT)
                    .httpOnly(true)
                    .build()
            )
            .build();
    }

    @GET
    @Path("/callback/connect-account")
    @RateLimited(bucket = "oauth")
    @Produces(MediaType.TEXT_PLAIN)
    @RunOnVirtualThread
    public String connectAccountCallback(
        @RestQuery Optional<String> code,
        @RestQuery Optional<String> state,
        @RestCookie(OAUTH2_COOKIE) Optional<String> cookie
    ) {
        if (code.isEmpty() || state.isEmpty() || cookie.isEmpty()) {
            log.error("Missing request params");
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        try {
            githubOauthService.finishConnectAccount(code.get(), state.get(), cookie.get());
        } catch (OauthStateValidationException | OauthFlowException e) {
            log.error("OAuth2 exception");
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } catch (GithubAccountAlreadyUsedException e) {
            log.error("Github account already associated with account");
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        return "OK";
    }
}
