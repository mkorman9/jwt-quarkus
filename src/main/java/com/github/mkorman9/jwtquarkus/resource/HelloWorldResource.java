package com.github.mkorman9.jwtquarkus.resource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/")
public class HelloWorldResource {
    @GET
    @Authenticated
    public String getHelloMessage(@Context JsonWebToken jwt) {
        return String.format("Hello %s (%s)", jwt.getName(), String.join(",", jwt.getGroups()));
    }
}
