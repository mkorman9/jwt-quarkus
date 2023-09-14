package com.github.mkorman9.jwtquarkus.accounts.dto;

import lombok.Builder;

@Builder
public record TokenPair(
    AccessToken accessToken,
    RefreshToken refreshToken
) {
}
