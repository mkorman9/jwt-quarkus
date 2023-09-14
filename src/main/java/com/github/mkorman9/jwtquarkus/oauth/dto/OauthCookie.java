package com.github.mkorman9.jwtquarkus.oauth.dto;

import lombok.Builder;

@Builder
public record OauthCookie(
    String cookie,
    String cookieHash
) {
}
