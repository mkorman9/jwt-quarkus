package com.github.mkorman9.jwtquarkus.oauth.service;

import com.github.mkorman9.jwtquarkus.accounts.dto.TokenPair;
import com.github.mkorman9.jwtquarkus.accounts.service.AccountService;
import com.github.mkorman9.jwtquarkus.accounts.service.TokenFacade;
import com.github.mkorman9.jwtquarkus.oauth.dto.GithubUserInfo;
import com.github.mkorman9.jwtquarkus.oauth.dto.OauthTicket;
import com.github.mkorman9.jwtquarkus.oauth.exception.GithubAccountAlreadyUsedException;
import com.github.mkorman9.jwtquarkus.oauth.exception.GithubAccountNotFoundException;
import com.github.mkorman9.jwtquarkus.oauth.exception.OauthStateValidationException;
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
    TokenFacade tokenFacade;

    @Inject
    GithubAPI githubAPI;

    @Inject
    AccountService accountService;

    public OauthTicket beginLogin() {
        var state = oauthStateService.generateState();
        var url = githubAPI.getLoginUrl(state.getState());

        return OauthTicket.builder()
                .url(url)
                .state(state)
                .build();
    }

    public OauthTicket beginConnectAccount(String accessToken) {
        var token = tokenFacade.validateAccessToken(accessToken);

        var state = oauthStateService.generateState(token.getSubject().toString());
        var url = githubAPI.getConnectAccountUrl(state.getState());

        return OauthTicket.builder()
                .url(url)
                .state(state)
                .build();
    }

    public TokenPair finishLogin(String code, String state, String cookie) {
        var validationResult = oauthStateService.validateState(state, cookie);
        if (!validationResult.isValid()) {
            throw new OauthStateValidationException();
        }

        var githubAccessToken = githubAPI.retrieveAccessToken(code);
        var userInfo = githubAPI.retrieveUserInfo(githubAccessToken);

        return loginToAccount(userInfo);
    }

    public void finishConnectAccount(String code, String state, String cookie) {
        var validationResult = oauthStateService.validateState(state, cookie);
        if (!validationResult.isValid()) {
            throw new OauthStateValidationException();
        }

        var githubAccessToken = githubAPI.retrieveAccessToken(code);
        var userInfo = githubAPI.retrieveUserInfo(githubAccessToken);
        var userId = UUID.fromString(validationResult.getSubject());

        connectAccount(userInfo, userId);
    }

    private TokenPair loginToAccount(GithubUserInfo userInfo) {
        var userId = accountService.getByGithubId(userInfo.getId());
        if (userId == null) {
            throw new GithubAccountNotFoundException();
        }

        log.info("User {} logged in as {} ({})", userId, userInfo.getName(), userInfo.getEmail());
        return tokenFacade.generatePair(userId);
    }

    private void connectAccount(GithubUserInfo userInfo, UUID userId) {
        if (accountService.getByGithubId(userInfo.getId()) != null) {
            throw new GithubAccountAlreadyUsedException();
        }

        accountService.connectGithubAccount(userInfo, userId);
    }
}
