package com.github.mkorman9.jwtquarkus.service;

import com.github.mkorman9.jwtquarkus.dto.OauthCookie;
import com.github.mkorman9.jwtquarkus.dto.OauthState;
import com.github.mkorman9.jwtquarkus.dto.OauthStateValidationResult;
import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Set;
import java.util.UUID;

@ApplicationScoped
@Slf4j
public class OauthStateService {
    private static final String STATE_AUDIENCE = "jwt-quarkus/oauth-state";

    @Inject
    JWTParser jwtParser;

    @Inject
    OauthCookieService oauthCookieService;

    private final JWTAuthContextInfo authContextInfo;

    @Inject
    public OauthStateService(JWTAuthContextInfo originalContextInfo) {
        this.authContextInfo = new JWTAuthContextInfo(originalContextInfo);
        this.authContextInfo.setExpectedAudience(Set.of(STATE_AUDIENCE));
    }

    public OauthState generateState(UUID userId) {
        var cookie = oauthCookieService.generateCookie();
        var state = Jwt.issuer("jwt-quarkus")
                .audience(STATE_AUDIENCE)
                .subject(userId.toString())
                .claim("cookie", cookie.getCookieHash())
                .expiresIn(300)
                .sign();

        return OauthState.builder()
                .state(state)
                .cookie(cookie.getCookie())
                .build();
    }

    public OauthStateValidationResult validateState(String state, String cookie) {
        JsonWebToken token;

        try {
            token = jwtParser.parse(state, authContextInfo);
        } catch (ParseException e) {
            log.error("Token parsing error", e);
            return OauthStateValidationResult.builder()
                    .valid(false)
                    .build();
        }

        var cookieHash = token.<String>getClaim("cookie");
        if (cookieHash == null) {
            return OauthStateValidationResult.builder()
                    .valid(false)
                    .build();
        }

        var cookieToValidate = OauthCookie.builder()
                .cookie(cookie)
                .cookieHash(cookieHash)
                .build();
        if (!oauthCookieService.validateCookie(cookieToValidate)) {
            return OauthStateValidationResult.builder()
                    .valid(false)
                    .build();
        }

        return OauthStateValidationResult.builder()
                .valid(true)
                .userId(UUID.fromString(token.getSubject()))
                .build();
    }
}
