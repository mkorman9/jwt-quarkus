package com.github.mkorman9.jwtquarkus.oauth.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.xml.bind.DatatypeConverter;
import lombok.Builder;
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
        var cookieHash = hashCookie(cookieValue);

        return OauthCookie.builder()
            .cookie(cookieValue)
            .cookieHash(cookieHash)
            .build();
    }

    public boolean validateCookie(String cookie, String cookieHash) {
        var validCookieHash = hashCookie(cookie);
        return validCookieHash.equals(cookieHash);
    }

    private String generateRandomCookie() {
        return RANDOM.ints(COOKIE_LENGTH, 0, COOKIE_CHARSET.length())
            .mapToObj(COOKIE_CHARSET::charAt)
            .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
            .toString();
    }

    private String hashCookie(String value) {
        return DatatypeConverter.printHexBinary(
            DIGEST.digest(value.getBytes(StandardCharsets.US_ASCII))
        );
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

    @Builder
    public record OauthCookie(
        String cookie,
        String cookieHash
    ) {
    }
}
