package com.github.mkorman9.jwtquarkus.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TokenRefreshPayload {
    private String accessToken;

    private String refreshToken;
}
