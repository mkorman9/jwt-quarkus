package com.github.mkorman9.jwtquarkus.token;

import com.github.mkorman9.jwtquarkus.token.exception.AccessTokenValidationException;
import com.github.mkorman9.jwtquarkus.token.exception.RefreshTokenValidationException;
import com.github.mkorman9.jwtquarkus.token.exception.TokenRefreshException;
import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class TokenService {
    private static final String ISSUER = "jwt-quarkus";
    private static final String ACCESS_TOKEN_AUDIENCE = "jwt-quarkus/access";
    private static final TemporalAmount ACCESS_TOKEN_DURATION = Duration.ofHours(12);
    private static final String REFRESH_TOKEN_AUDIENCE = "jwt-quarkus/refresh";
    private static final TemporalAmount REFRESH_TOKEN_DURATION = Period.ofYears(10);

    @Inject
    JWTParser jwtParser;

    private final JWTAuthContextInfo accessTokenContextForRefresh;
    private final JWTAuthContextInfo refreshTokenContextForRefresh;

    @Inject
    public TokenService(JWTAuthContextInfo originalContextInfo) {
        this.accessTokenContextForRefresh = new JWTAuthContextInfo(originalContextInfo);
        this.accessTokenContextForRefresh.setClockSkew(Integer.MAX_VALUE);
        this.refreshTokenContextForRefresh = new JWTAuthContextInfo(originalContextInfo);
        this.refreshTokenContextForRefresh.setExpectedAudience(Set.of(REFRESH_TOKEN_AUDIENCE));
    }

    public TokenPair generatePair(UUID userId) {
        var accessToken = generateAccessToken(userId);
        var refreshToken = generateRefreshToken(accessToken);

        return TokenPair.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
    }

    public TokenPair refreshToken(String accessToken, String refreshToken) {
        JsonWebToken accessTokenParsed;
        try {
            accessTokenParsed = jwtParser.parse(accessToken, accessTokenContextForRefresh);
        } catch (ParseException e) {
            throw new AccessTokenValidationException(e);
        }

        JsonWebToken refreshTokenParsed;
        try {
            refreshTokenParsed = jwtParser.parse(refreshToken, refreshTokenContextForRefresh);
        } catch (ParseException e) {
            throw new RefreshTokenValidationException(e);
        }

        if (!refreshTokenParsed.getSubject().equals(accessTokenParsed.getTokenID())) {
            throw new TokenRefreshException("Subjects mismatch");
        }

        var userId = UUID.fromString(accessTokenParsed.getSubject());
        return generatePair(userId);
    }

    public AccessToken validateAccessToken(String accessToken) {
        try {
            var token = jwtParser.parse(accessToken);

            return AccessToken.builder()
                .token(accessToken)
                .subject(UUID.fromString(token.getSubject()))
                .expiresAt(Instant.ofEpochMilli(token.getExpirationTime()))
                .build();
        } catch (ParseException e) {
            throw new AccessTokenValidationException(e);
        }
    }

    private AccessToken generateAccessToken(UUID userId) {
        var expiresAt = Instant.now().plus(ACCESS_TOKEN_DURATION);
        var token = Jwt.issuer(ISSUER)
            .audience(ACCESS_TOKEN_AUDIENCE)
            .subject(userId.toString())
            .expiresAt(expiresAt)
            .sign();

        return AccessToken.builder()
            .token(token)
            .subject(userId)
            .expiresAt(expiresAt)
            .build();
    }

    private RefreshToken generateRefreshToken(AccessToken source) {
        JsonWebToken accessToken;

        try {
            accessToken = jwtParser.parse(source.token());
        } catch (ParseException e) {
            throw new AccessTokenValidationException(e);
        }

        var accessTokenId = accessToken.getTokenID();
        var refreshToken = Jwt.issuer(ISSUER)
            .audience(REFRESH_TOKEN_AUDIENCE)
            .subject(accessTokenId)
            .expiresAt(ZonedDateTime.now(ZoneOffset.UTC).plus(REFRESH_TOKEN_DURATION).toInstant())
            .sign();

        return RefreshToken.builder()
            .token(refreshToken)
            .build();
    }
}
