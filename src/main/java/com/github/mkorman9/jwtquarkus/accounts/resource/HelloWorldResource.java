package com.github.mkorman9.jwtquarkus.accounts.resource;

import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/")
@Produces(MediaType.TEXT_PLAIN)
@RunOnVirtualThread
public class HelloWorldResource {
    @GET
    @Authenticated
    public String getHelloMessage(@Context JsonWebToken jwt) {
        return String.format("Hello %s", jwt.getName());
    }
}
