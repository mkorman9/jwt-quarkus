package com.github.mkorman9.jwtquarkus.token;

import com.github.mkorman9.jwtquarkus.token.AccessToken;
import com.github.mkorman9.jwtquarkus.token.exception.AccessTokenValidationException;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.UUID;

@ApplicationScoped
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
            .subject(userId)
            .expiresAt(expiresAt)
            .build();
    }

    public AccessToken validate(String accessToken) {
        JsonWebToken token;

        try {
            token = jwtParser.parse(accessToken);
        } catch (ParseException e) {
            throw new AccessTokenValidationException(e);
        }

        return AccessToken.builder()
            .token(accessToken)
            .subject(UUID.fromString(token.getSubject()))
            .expiresAt(Instant.ofEpochMilli(token.getExpirationTime()))
            .build();
    }
}
