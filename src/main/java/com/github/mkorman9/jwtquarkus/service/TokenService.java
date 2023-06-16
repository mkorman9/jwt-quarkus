package com.github.mkorman9.jwtquarkus.service;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashSet;
import java.util.List;

@ApplicationScoped
public class TokenService {
    public String generate() {
        return Jwt.issuer("jwt-quarkus")
                .subject("michal")
                .groups(new HashSet<>(List.of("users")))
                .sign();
    }
}
