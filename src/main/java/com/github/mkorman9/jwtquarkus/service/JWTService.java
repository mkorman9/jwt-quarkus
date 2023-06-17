package com.github.mkorman9.jwtquarkus.service;

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
public class JWTService {
    private static final String ISSUER = "jwt-quarkus";
    private static final String ACCESS_TOKEN_AUDIENCE = "jwt-quarkus/access";
    private static final String OAUTH_STATE_AUDIENCE = "jwt-quarkus/oauth-state";

    @Inject
    JWTParser jwtParser;

    private final JWTAuthContextInfo oauthStateAuthContextInfo;

    @Inject
    public JWTService(JWTAuthContextInfo originalContextInfo) {
        this.oauthStateAuthContextInfo = new JWTAuthContextInfo(originalContextInfo);
        this.oauthStateAuthContextInfo.setExpectedAudience(Set.of(OAUTH_STATE_AUDIENCE));
    }

    public String generateAccessToken() {
        return Jwt.issuer(ISSUER)
                .audience(ACCESS_TOKEN_AUDIENCE)
                .subject("michal")
                .groups(Set.of("users"))
                .sign();
    }

    public String generateOauthState() {
        return Jwt.issuer(ISSUER)
                .audience(OAUTH_STATE_AUDIENCE)
                .subject("")
                .expiresIn(300)
                .sign();
    }

    public boolean validateOauthState(String state) {
        try {
            jwtParser.parse(state, oauthStateAuthContextInfo);
        } catch (ParseException e) {
            log.error("Token parsing error", e);
            return false;
        }

        return true;
    }
}
