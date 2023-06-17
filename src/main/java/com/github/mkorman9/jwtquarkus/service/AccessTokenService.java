package com.github.mkorman9.jwtquarkus.service;

import com.github.mkorman9.jwtquarkus.dto.AccessToken;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@ApplicationScoped
@Slf4j
public class AccessTokenService {
    public AccessToken generate(String subject) {
        var token = Jwt.issuer("jwt-quarkus")
                .audience("jwt-quarkus/access")
                .subject(subject)
                .groups(Set.of("users"))
                .sign();

        return AccessToken.builder()
                .token(token)
                .build();
    }
}
