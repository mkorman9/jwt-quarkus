package com.github.mkorman9.jwtquarkus.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class GithubUserInfo {
    private long id;

    private String login;

    private String email;

    private String name;

    private String avatarUrl;
}
