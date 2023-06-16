package com.github.mkorman9.jwtquarkus.resource;

import com.github.mkorman9.jwtquarkus.service.TokenService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/")
public class HelloWorldResource {
    @Inject
    TokenService tokenService;

    @GET
    @Authenticated
    public String getHello(@Context JsonWebToken jwt) {
        return String.format("Hello %s (%s)", jwt.getName(), String.join(",", jwt.getGroups()));
    }

    @GET
    @Path("/token")
    @PermitAll
    public String getToken() {
        return tokenService.generate();
    }
}
