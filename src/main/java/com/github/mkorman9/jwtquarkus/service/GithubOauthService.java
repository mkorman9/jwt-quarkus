package com.github.mkorman9.jwtquarkus.service;

import com.github.mkorman9.jwtquarkus.dto.AccessToken;
import com.github.mkorman9.jwtquarkus.dto.GithubAuthorizationResult;
import com.github.mkorman9.jwtquarkus.dto.GithubUserInfo;
import com.github.mkorman9.jwtquarkus.dto.OauthAuthorization;
import com.github.mkorman9.jwtquarkus.exception.GithubAccountAlreadyUsedException;
import com.github.mkorman9.jwtquarkus.exception.GithubAccountNotFoundException;
import com.github.mkorman9.jwtquarkus.exception.OauthStateValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
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

    @Inject
    AccountService accountService;

    public OauthAuthorization beginAccountLogin() {
        var state = oauthStateService.generateState(Optional.empty());
        var url = githubAPI.getAuthorizationUrl(state.getState());

        return OauthAuthorization.builder()
                .url(url)
                .state(state)
                .build();
    }

    public OauthAuthorization beginAccountConnecting(String accessToken) {
        var userId = accessTokenService.extractUserId(accessToken);

        var state = oauthStateService.generateState(Optional.of(userId));
        var url = githubAPI.getAuthorizationUrl(state.getState());

        return OauthAuthorization.builder()
                .url(url)
                .state(state)
                .build();
    }

    public GithubAuthorizationResult finishAuthorization(String code, String state, String cookie) {
        var validationResult = oauthStateService.validateState(state, cookie);
        if (!validationResult.isValid()) {
            throw new OauthStateValidationException();
        }

        var githubAccessToken = githubAPI.retrieveAccessToken(code);
        var userInfo = githubAPI.retrieveUserInfo(githubAccessToken);

        if (!validationResult.getSubject().isEmpty()) {
            return GithubAuthorizationResult.builder()
                    .action(GithubAuthorizationResult.Action.CONNECT_ACCOUNT)
                    .userInfo(userInfo)
                    .userId(UUID.fromString(validationResult.getSubject()))
                    .build();
        } else {
            return GithubAuthorizationResult.builder()
                    .action(GithubAuthorizationResult.Action.LOGIN)
                    .userInfo(userInfo)
                    .build();
        }
    }

    public void connectAccount(GithubUserInfo userInfo, UUID userId) {
        if (accountService.getByGithubId(userInfo.getId()) != null) {
            throw new GithubAccountAlreadyUsedException();
        }

        log.info("User {} connected account {} ({})", userId, userInfo.getName(), userInfo.getEmail());
        accountService.connectAccount(userInfo.getId(), userId);
    }

    public AccessToken loginToAccount(GithubUserInfo userInfo) {
        var userId = accountService.getByGithubId(userInfo.getId());
        if (userId == null) {
            throw new GithubAccountNotFoundException();
        }

        log.info("User {} logged in as {} ({})", userId, userInfo.getName(), userInfo.getEmail());
        return accessTokenService.generate(userId);
    }
}
