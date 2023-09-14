package com.github.mkorman9.jwtquarkus.accounts.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record AccessToken(
    String token,
    UUID subject,
    Instant expiresAt
) {
}
