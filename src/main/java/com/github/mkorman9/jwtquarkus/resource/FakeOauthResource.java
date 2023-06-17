package com.github.mkorman9.jwtquarkus.resource;

import com.github.mkorman9.jwtquarkus.service.JWTService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestQuery;

import java.util.Optional;

@Path("/oauth")
public class FakeOauthResource {
    @Inject
    JWTService jwtService;

    @GET
    @Path("/state")
    public String getNewState() {
        return jwtService.generateOauthState();
    }

    @GET
    @Path("/callback")
    public String getNewState(@RestQuery("state") Optional<String> state) {
        if (state.isEmpty()) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        return jwtService.validateOauthState(state.get()) ? "OK" : "NOT OK";
    }
}
