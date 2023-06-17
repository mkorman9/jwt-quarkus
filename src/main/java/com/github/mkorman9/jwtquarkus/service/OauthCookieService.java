package com.github.mkorman9.jwtquarkus.service;

import com.github.mkorman9.jwtquarkus.dto.OauthCookie;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.xml.bind.DatatypeConverter;
import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

@ApplicationScoped
public class OauthCookieService {
    private static final String COOKIE_CHARSET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int COOKIE_LENGTH = 16;
    private static final SecureRandom RANDOM = selectSecureRandom();
    private static final MessageDigest DIGEST = createMessageDigest();

    public OauthCookie generateCookie() {
        var cookieValue = generateRandomCookie();
        var cookieHash = DatatypeConverter.printHexBinary(
                DIGEST.digest(cookieValue.getBytes(StandardCharsets.US_ASCII))
        );

        return OauthCookie.builder()
                .cookie(cookieValue)
                .cookieHash(cookieHash)
                .build();
    }

    public boolean validateCookie(OauthCookie cookie) {
        var validCookieHash = DatatypeConverter.printHexBinary(
                DIGEST.digest(cookie.getCookie().getBytes(StandardCharsets.US_ASCII))
        );

        return validCookieHash.equals(cookie.getCookieHash());
    }

    private String generateRandomCookie() {
        return RANDOM.ints(COOKIE_LENGTH, 0, COOKIE_CHARSET.length())
                .mapToObj(COOKIE_CHARSET::charAt)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }

    @SneakyThrows
    private static SecureRandom selectSecureRandom() {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return SecureRandom.getInstanceStrong();
        } else {
            return SecureRandom.getInstance("NativePRNGNonBlocking");
        }
    }

    @SneakyThrows
    private static MessageDigest createMessageDigest() {
        return MessageDigest.getInstance("SHA-256");
    }
}
