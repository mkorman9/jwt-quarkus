package com.github.mkorman9.jwtquarkus.resource;

import com.github.mkorman9.jwtquarkus.service.JWTService;
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
    JWTService jwtService;

    @GET
    @Authenticated
    public String getHelloMessage(@Context JsonWebToken jwt) {
        return String.format("Hello %s (%s)", jwt.getName(), String.join(",", jwt.getGroups()));
    }

    @GET
    @Path("/token")
    @PermitAll
    public String getAccessToken() {
        return jwtService.generateAccessToken();
    }
}
