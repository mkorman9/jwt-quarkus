package com.github.mkorman9.jwtquarkus.resource;

import com.github.mkorman9.jwtquarkus.service.GithubOauthService;
import com.github.mkorman9.jwtquarkus.service.JWTService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestQuery;

import java.net.URI;
import java.util.Optional;

@Path("/oauth")
public class OauthResource {
    @Inject
    JWTService jwtService;

    @Inject
    GithubOauthService githubOauthService;

    @GET
    @Path("/auth")
    public Response authorize() {
        return Response
                .seeOther(URI.create(githubOauthService.getAuthorizationUrl()))
                .build();
    }

    @GET
    @Path("/callback")
    public String callback(
            @RestQuery Optional<String> code,
            @RestQuery Optional<String> state
    ) {
        if (code.isEmpty() || state.isEmpty()) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        var maybeAccessToken = githubOauthService.retrieveAccessToken(code.get(), state.get());
        if (maybeAccessToken.isEmpty()) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        var accessToken = maybeAccessToken.get();
        var userInfo = githubOauthService.retrieveUserInfo(accessToken);

        return jwtService.generateAccessToken(userInfo.getName());
    }
}
