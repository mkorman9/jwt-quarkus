package com.github.mkorman9.jwtquarkus.resource;

import com.github.mkorman9.jwtquarkus.dto.AccessToken;
import com.github.mkorman9.jwtquarkus.service.AccessTokenService;
import com.github.mkorman9.jwtquarkus.service.GithubOauthService;
import com.github.mkorman9.jwtquarkus.service.OauthService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestCookie;
import org.jboss.resteasy.reactive.RestQuery;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Path("/oauth")
public class OauthResource {
    private static final String OAUTH2_COOKIE = "oauth2_cookie";

    @Inject
    OauthService oauthService;

    @Inject
    AccessTokenService accessTokenService;

    @Inject
    GithubOauthService githubOauthService;

    @GET
    @Path("/auth")
    public Response authorize() {
        var state = oauthService.generateState();

        return Response
                .seeOther(URI.create(
                        githubOauthService.getAuthorizationUrl(state.getState())
                ))
                .cookie(
                        new NewCookie.Builder(OAUTH2_COOKIE)
                                .value(state.getCookie())
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
    @Path("/callback")
    public Response callback(
            @RestQuery Optional<String> code,
            @RestQuery Optional<String> state,
            @RestCookie(OAUTH2_COOKIE) Optional<String> cookie
    ) {
        if (code.isEmpty() || state.isEmpty() || cookie.isEmpty()) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        if (!oauthService.validateState(state.get(), cookie.get())) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        var maybeAccessToken = githubOauthService.retrieveAccessToken(code.get());
        if (maybeAccessToken.isEmpty()) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        var accessToken = maybeAccessToken.get();
        var userInfo = githubOauthService.retrieveUserInfo(accessToken);
        var token = accessTokenService.generate(userInfo.getName());

        return Response
                .ok("OK")
                .cookie(
                        new NewCookie.Builder("access_token")
                                .value(token.getToken())
                                .sameSite(NewCookie.SameSite.STRICT)
                                .httpOnly(true)
                                .build()
                )
                .build();
    }
}
