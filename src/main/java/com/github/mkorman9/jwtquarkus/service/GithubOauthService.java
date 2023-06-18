package com.github.mkorman9.jwtquarkus.service;

import com.github.mkorman9.jwtquarkus.dto.AccessToken;
import com.github.mkorman9.jwtquarkus.dto.OauthAuthorization;
import com.github.mkorman9.jwtquarkus.exception.OauthStateValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@ApplicationScoped
@Slf4j
public class GithubOauthService {
    @Inject
    OauthStateService oauthStateService;

    @Inject
    AccessTokenService accessTokenService;

    @Inject
    GithubAPI githubAPI;

    public OauthAuthorization beginAuthorization(UUID userId) {
        var state = oauthStateService.generateState(userId);
        var url = githubAPI.getAuthorizationUrl(state.getState());

        return OauthAuthorization.builder()
                .url(url)
                .state(state)
                .build();
    }

    public AccessToken finishAuthorization(String code, String state, String cookie) {
        var validationResult = oauthStateService.validateState(state, cookie);
        if (!validationResult.isValid()) {
            throw new OauthStateValidationException();
        }

        var githubAccessToken = githubAPI.retrieveAccessToken(code);
        var userInfo = githubAPI.retrieveUserInfo(githubAccessToken);
        var userId = validationResult.getUserId().toString();

        log.info("User {} authorized as {} ({})", userId, userInfo.getName(), userInfo.getEmail());

        return accessTokenService.generate(userId);
    }
}
