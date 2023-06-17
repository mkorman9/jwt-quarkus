package com.github.mkorman9.jwtquarkus.resource;

import com.github.mkorman9.jwtquarkus.dto.AccessToken;
import com.github.mkorman9.jwtquarkus.service.AccessTokenService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/admin")
public class AdminController {
    @Inject
    AccessTokenService accessTokenService;

    @GET
    @Path("/token")
    public AccessToken getAccessToken() {
        return accessTokenService.generate("admin");
    }
}
