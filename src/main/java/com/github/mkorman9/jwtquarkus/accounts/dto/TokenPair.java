package com.github.mkorman9.jwtquarkus.accounts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenPair {
    private AccessToken accessToken;

    private RefreshToken refreshToken;
}
