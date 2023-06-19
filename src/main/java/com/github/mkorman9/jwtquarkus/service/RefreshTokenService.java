package com.github.mkorman9.jwtquarkus.service;

import com.github.mkorman9.jwtquarkus.dto.AccessToken;
import com.github.mkorman9.jwtquarkus.dto.RefreshToken;
import com.github.mkorman9.jwtquarkus.dto.TokenRefreshResult;
import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.Period;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
@Slf4j
public class RefreshTokenService {
    private static final String REFRESH_AUDIENCE = "jwt-quarkus/refresh";
    private static final TemporalAmount REFRESH_TOKEN_DURATION = Period.ofYears(10);

    @Inject
    JWTParser jwtParser;

    private final JWTAuthContextInfo accessTokenContextInfo;
    private final JWTAuthContextInfo refreshTokenContextInfo;

    @Inject
    public RefreshTokenService(JWTAuthContextInfo originalContextInfo) {
        this.accessTokenContextInfo = new JWTAuthContextInfo(originalContextInfo);
        this.accessTokenContextInfo.setClockSkew(Integer.MAX_VALUE);
        this.refreshTokenContextInfo = new JWTAuthContextInfo(originalContextInfo);
        this.refreshTokenContextInfo.setExpectedAudience(Set.of(REFRESH_AUDIENCE));
    }

    public RefreshToken generate(AccessToken source) {
        JsonWebToken token;

        try {
            token = jwtParser.parse(source.getToken());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        var tokenId = token.getTokenID();
        var refreshToken = Jwt.issuer("jwt-quarkus")
                .audience(REFRESH_AUDIENCE)
                .subject(tokenId)
                .expiresAt(ZonedDateTime.now(ZoneOffset.UTC).plus(REFRESH_TOKEN_DURATION).toInstant())
                .sign();

        return RefreshToken.builder()
                .token(refreshToken)
                .build();
    }

    public TokenRefreshResult refresh(String refreshToken, String accessToken) {
        JsonWebToken accessTokenParsed;
        JsonWebToken refreshTokenParsed;

        try {
            accessTokenParsed = jwtParser.parse(accessToken, accessTokenContextInfo);
            refreshTokenParsed = jwtParser.parse(refreshToken, refreshTokenContextInfo);
        } catch (ParseException e) {
            log.error("Refresh token parsing exception", e);
            return TokenRefreshResult.builder()
                    .valid(false)
                    .build();
        }

        if (!refreshTokenParsed.getSubject().equals(accessTokenParsed.getTokenID())) {
            return TokenRefreshResult.builder()
                    .valid(false)
                    .build();
        }

        return TokenRefreshResult.builder()
                .valid(true)
                .userId(UUID.fromString(accessTokenParsed.getSubject()))
                .build();
    }
}
