package com.github.mkorman9.jwtquarkus.oauth.dto;

import lombok.Builder;

@Builder
public record GithubUserInfo(
    long id,
    String login,
    String email,
    String name,
    String avatarUrl
) {
}
