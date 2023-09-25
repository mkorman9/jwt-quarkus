package com.github.mkorman9.jwtquarkus.token;

import lombok.Builder;

@Builder
public record RefreshToken(
    String token
) {
}
