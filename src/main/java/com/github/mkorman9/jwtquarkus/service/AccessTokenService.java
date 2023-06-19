package com.github.mkorman9.jwtquarkus.service;

import com.github.mkorman9.jwtquarkus.dto.AccessToken;
import com.github.mkorman9.jwtquarkus.exception.AccessTokenValidationException;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.UUID;

@ApplicationScoped
@Slf4j
public class AccessTokenService {
    private static final String ACCESS_AUDIENCE = "jwt-quarkus/access";
    private static final TemporalAmount ACCESS_TOKEN_DURATION = Duration.ofHours(12);

    @Inject
    JWTParser jwtParser;

    public AccessToken generate(UUID userId) {
        var expiresAt = Instant.now().plus(ACCESS_TOKEN_DURATION);
        var token = Jwt.issuer("jwt-quarkus")
                .audience(ACCESS_AUDIENCE)
                .subject(userId.toString())
                .expiresAt(expiresAt)
                .sign();

        return AccessToken.builder()
                .token(token)
                .subject(userId.toString())
                .expiresAt(expiresAt)
                .build();
    }

    public UUID extractUserId(String accessToken) {
        JsonWebToken token;

        try {
            token = jwtParser.parse(accessToken);
        } catch (ParseException e) {
            log.error("Access token validation error", e);
            throw new AccessTokenValidationException();
        }

        return UUID.fromString(token.getSubject());
    }
}
