package com.github.mkorman9.jwtquarkus.resource;

import com.github.mkorman9.jwtquarkus.dto.AccessToken;
import com.github.mkorman9.jwtquarkus.exception.OauthFlowException;
import com.github.mkorman9.jwtquarkus.exception.OauthStateValidationException;
import com.github.mkorman9.jwtquarkus.service.GithubOauthService;
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
import java.util.UUID;

@Path("/oauth")
public class OauthResource {
    private static final String OAUTH2_COOKIE = "oauth2_cookie";

    @Inject
    GithubOauthService githubOauthService;

    @GET
    @Path("/auth")
    public Response authorize() {
        var userId = UUID.randomUUID();
        var auth = githubOauthService.beginAuthorization(userId);

        return Response
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
    @Path("/callback")
    public Response callback(
            @RestQuery Optional<String> code,
            @RestQuery Optional<String> state,
            @RestCookie(OAUTH2_COOKIE) Optional<String> cookie
    ) {
        if (code.isEmpty() || state.isEmpty() || cookie.isEmpty()) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        AccessToken token;

        try {
            token = githubOauthService.finishAuthorization(code.get(), state.get(), cookie.get());
        } catch (OauthStateValidationException | OauthFlowException e) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

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
