package com.github.mkorman9.jwtquarkus.oauth;

import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@ApplicationScoped
@Slf4j
public class OauthStateService {
    private static final String STATE_AUDIENCE = "jwt-quarkus/oauth-state";
    private static final Duration STATE_DURATION = Duration.ofMinutes(5);

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

    public OauthState generateState() {
        return generateState("");
    }

    public OauthState generateState(String subject) {
        var cookie = oauthCookieService.generateCookie();
        var state = Jwt.issuer("jwt-quarkus")
            .audience(STATE_AUDIENCE)
            .subject(subject)
            .claim("cookie", cookie.cookieHash())
            .expiresIn(Instant.now().plus(STATE_DURATION).toEpochMilli())
            .sign();

        return OauthState.builder()
            .state(state)
            .cookie(cookie.cookie())
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

        if (!oauthCookieService.validateCookie(cookie, cookieHash)) {
            return OauthStateValidationResult.builder()
                .valid(false)
                .build();
        }

        return OauthStateValidationResult.builder()
            .valid(true)
            .subject(token.getSubject())
            .build();
    }

    @Builder
    public record OauthStateValidationResult(
        boolean valid,
        String subject
    ) {
    }
}
