package com.github.mkorman9.jwtquarkus.service;

import com.github.mkorman9.jwtquarkus.dto.AccessToken;
import com.github.mkorman9.jwtquarkus.dto.RefreshToken;
import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Set;

@ApplicationScoped
@Slf4j
public class RefreshTokenService {
    private static final String REFRESH_AUDIENCE = "jwt-quarkus/refresh";
    private static final int REFRESH_TOKEN_DURATION = 315360000;  // 10 years

    @Inject
    JWTParser jwtParser;

    private final JWTAuthContextInfo refreshTokenContextInfo;

    @Inject
    public RefreshTokenService(JWTAuthContextInfo originalContextInfo) {
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
                .expiresIn(REFRESH_TOKEN_DURATION)
                .sign();

        return RefreshToken.builder()
                .token(refreshToken)
                .build();
    }

    public boolean validateRefreshToken(String refreshToken, String accessToken) {
        JsonWebToken accessTokenParsed;
        JsonWebToken refreshTokenParsed;

        try {
            refreshTokenParsed = jwtParser.parse(refreshToken, refreshTokenContextInfo);
            accessTokenParsed = jwtParser.parse(accessToken);
        } catch (ParseException e) {
            log.error("Refresh token parsing exception", e);
            return false;
        }

        if (!refreshTokenParsed.getSubject().equals(accessTokenParsed.getTokenID())) {
            return false;
        }

        return true;
    }
}
