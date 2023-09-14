package com.github.mkorman9.jwtquarkus.oauth.dto.payload;

public record GithubUserEmailResponse(
    String email,
    boolean verified,
    boolean primary
) {
}
