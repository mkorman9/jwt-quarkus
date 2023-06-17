package com.github.mkorman9.jwtquarkus.service;

import com.github.mkorman9.jwtquarkus.dto.AccessToken;
import com.github.mkorman9.jwtquarkus.dto.OauthAuthorization;
import com.github.mkorman9.jwtquarkus.exception.OauthStateValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GithubOauthService {
    @Inject
    OauthStateService oauthStateService;

    @Inject
    AccessTokenService accessTokenService;

    @Inject
    GithubAPI githubAPI;

    public OauthAuthorization beginAuthorization() {
        var state = oauthStateService.generateState();
        var url = githubAPI.getAuthorizationUrl(state.getState());

        return OauthAuthorization.builder()
                .url(url)
                .state(state)
                .build();
    }

    public AccessToken finishAuthorization(String code, String state, String cookie) {
        if (!oauthStateService.validateState(state, cookie)) {
            throw new OauthStateValidationException();
        }

        var githubAccessToken = githubAPI.retrieveAccessToken(code);
        var userInfo = githubAPI.retrieveUserInfo(githubAccessToken);

        return accessTokenService.generate(userInfo.getName());
    }
}
