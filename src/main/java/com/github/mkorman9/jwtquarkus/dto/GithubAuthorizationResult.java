package com.github.mkorman9.jwtquarkus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GithubAuthorizationResult {
    private GithubUserInfo userInfo;

    private UUID userId;

    private Action action;

    public enum Action {
        LOGIN,
        CONNECT_ACCOUNT
    }
}
