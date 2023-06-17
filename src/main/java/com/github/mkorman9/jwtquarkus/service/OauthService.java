package com.github.mkorman9.jwtquarkus.service;

import com.github.mkorman9.jwtquarkus.dto.OauthCookie;
import com.github.mkorman9.jwtquarkus.dto.OauthState;
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
public class OauthService {
    private static final String STATE_AUDIENCE = "jwt-quarkus/oauth-state";

    @Inject
    JWTParser jwtParser;

    @Inject
    OauthCookieService oauthCookieService;

    private final JWTAuthContextInfo authContextInfo;

    @Inject
    public OauthService(JWTAuthContextInfo originalContextInfo) {
        this.authContextInfo = new JWTAuthContextInfo(originalContextInfo);
        this.authContextInfo.setExpectedAudience(Set.of(STATE_AUDIENCE));
    }

    public OauthState generateState() {
        var cookie = oauthCookieService.generateCookie();
        var state = Jwt.issuer("jwt-quarkus")
                .audience(STATE_AUDIENCE)
                .subject("")
                .claim("cookie", cookie.getCookieHash())
                .expiresIn(300)
                .sign();

        return OauthState.builder()
                .state(state)
                .cookie(cookie.getCookie())
                .build();
    }

    public boolean validateState(String state, String cookie) {
        JsonWebToken token;

        try {
            token = jwtParser.parse(state, authContextInfo);
        } catch (ParseException e) {
            log.error("Token parsing error", e);
            return false;
        }

        var cookieHash = token.<String>getClaim("cookie");
        if (cookieHash == null) {
            return false;
        }

        return oauthCookieService.validateCookie(
                OauthCookie.builder()
                        .cookie(cookie)
                        .cookieHash(cookieHash)
                        .build()
        );
    }
}
