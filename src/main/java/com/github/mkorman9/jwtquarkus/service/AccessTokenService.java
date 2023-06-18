package com.github.mkorman9.jwtquarkus.service;

import com.github.mkorman9.jwtquarkus.dto.AccessToken;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;

@ApplicationScoped
@Slf4j
public class AccessTokenService {
    private static final TemporalAmount ACCESS_TOKEN_DURATION = Duration.ofHours(12);

    public AccessToken generate(String subject) {
        var expiresAt = Instant.now().plus(ACCESS_TOKEN_DURATION);
        var token = Jwt.issuer("jwt-quarkus")
                .audience("jwt-quarkus/access")
                .subject(subject)
                .expiresAt(expiresAt)
                .sign();

        return AccessToken.builder()
                .token(token)
                .subject(subject)
                .expiresAt(expiresAt)
                .build();
    }
}
