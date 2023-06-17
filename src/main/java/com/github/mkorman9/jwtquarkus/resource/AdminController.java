package com.github.mkorman9.jwtquarkus.resource;

import com.github.mkorman9.jwtquarkus.service.JWTService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/admin")
public class AdminController {
    @Inject
    JWTService jwtService;

    @GET
    @Path("/token")
    public String getAccessToken() {
        return jwtService.generateAccessToken("admin");
    }
}
