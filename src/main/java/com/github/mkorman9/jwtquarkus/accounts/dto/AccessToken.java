package com.github.mkorman9.jwtquarkus.accounts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessToken {
    private String token;

    private UUID subject;

    private Instant expiresAt;
}
