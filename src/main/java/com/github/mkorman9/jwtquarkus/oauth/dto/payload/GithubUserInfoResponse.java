package com.github.mkorman9.jwtquarkus.oauth.dto.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubUserInfoResponse(
    long id,
    String login,
    String name,
    @JsonProperty("avatar_url") String avatarUrl
) {
}
