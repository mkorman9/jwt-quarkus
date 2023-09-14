package com.github.mkorman9.jwtquarkus.accounts.dto;

import lombok.Builder;

@Builder
public record RefreshToken(
    String token
) {
}

