package com.github.mkorman9.jwtquarkus.accounts.dto.payload;

import lombok.Builder;

@Builder
public record AccountResponse(
    String id,
    TokenResponse token
) {
}
