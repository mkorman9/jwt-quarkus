package com.github.mkorman9.jwtquarkus.helloworld;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/")
@Produces(MediaType.TEXT_PLAIN)
public class HelloWorldResource {
    @GET
    @Authenticated
    public String getHelloMessage(@Context JsonWebToken jwt) {
        return String.format("Hello %s", jwt.getName());
    }
}
