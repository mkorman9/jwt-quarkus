package com.github.mkorman9.jwtquarkus.oauth.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mkorman9.jwtquarkus.oauth.dto.GithubUserInfo;
import com.github.mkorman9.jwtquarkus.oauth.dto.payload.GithubUserEmailResponse;
import com.github.mkorman9.jwtquarkus.oauth.dto.payload.GithubUserInfoResponse;
import com.github.mkorman9.jwtquarkus.oauth.exception.OauthFlowException;
import com.github.scribejava.apis.GitHubApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutionException;

@ApplicationScoped
@Slf4j
public class GithubAPI {
    private static final String USER_INFO_URL = "https://api.github.com/user";
    private static final String USER_EMAIL_URL = "https://api.github.com/user/public_emails";
    private static final String EMAIL_SCOPE = "user:email";

    @Inject
    ObjectMapper objectMapper;

    private final OAuth20Service loginService;
    private final OAuth20Service connectAccountService;

    @Inject
    public GithubAPI(
            @ConfigProperty(name="oauth2.clientId") String clientId,
            @ConfigProperty(name="oauth2.clientSecret") String clientSecret,
            @ConfigProperty(name="oauth2.loginRedirectUrl") String loginRedirectUrl,
            @ConfigProperty(name="oauth2.connectAccountRedirectUrl") String connectAccountRedirectUrl
    ) {
        this.loginService = new ServiceBuilder(clientId)
                .apiSecret(clientSecret)
                .callback(loginRedirectUrl)
                .build(GitHubApi.instance());
        this.connectAccountService = new ServiceBuilder(clientId)
                .apiSecret(clientSecret)
                .callback(connectAccountRedirectUrl)
                .build(GitHubApi.instance());
    }

    public URI getLoginUrl(String state) {
        var url = loginService.createAuthorizationUrlBuilder()
                .state(state)
                .scope(EMAIL_SCOPE)
                .build();
        return URI.create(url);
    }

    public URI getConnectAccountUrl(String state) {
        var url = connectAccountService.createAuthorizationUrlBuilder()
                .state(state)
                .scope(EMAIL_SCOPE)
                .build();
        return URI.create(url);
    }

    @SneakyThrows
    public OAuth2AccessToken retrieveAccessToken(String code) {
        try {
            return loginService.getAccessToken(code);
        } catch (OAuthException e) {
            log.error("Error while retrieving GitHub access token", e);
            throw new OauthFlowException();
        }
    }

    public GithubUserInfo retrieveUserInfo(OAuth2AccessToken accessToken) {
        var userInfo = retrieveUserInfoResponse(accessToken);
        var userEmails = retrieveUserEmailResponses(accessToken);
        return GithubUserInfo.builder()
                .id(userInfo.id())
                .login(userInfo.login())
                .name(userInfo.name())
                .email(selectUserEmail(userEmails))
                .avatarUrl(userInfo.avatarUrl())
                .build();
    }

    private static String selectUserEmail(List<GithubUserEmailResponse> userEmails) {
        String candidate = "";
        for (var email : userEmails) {
            if (email.primary() && email.verified()) {
                return email.email();  // return primary, verified email immediately
            }

            if (email.verified()) {
                candidate = email.email();
            }
        }

        return candidate;
    }

    private GithubUserInfoResponse retrieveUserInfoResponse(OAuth2AccessToken accessToken) {
        var request = new OAuthRequest(Verb.GET, USER_INFO_URL);
        loginService.signRequest(accessToken, request);

        try (var response = loginService.execute(request)) {
            var body = response.getBody();
            return objectMapper.readValue(body, GithubUserInfoResponse.class);
        } catch (IOException | ExecutionException | InterruptedException e) {
            log.error("Error while retrieving GitHub user info", e);
            throw new OauthFlowException();
        }
    }

    private List<GithubUserEmailResponse> retrieveUserEmailResponses(OAuth2AccessToken accessToken) {
        var request = new OAuthRequest(Verb.GET, USER_EMAIL_URL);
        loginService.signRequest(accessToken, request);

        try (var response = loginService.execute(request)) {
            var body = response.getBody();
            return objectMapper.readValue(body, new TypeReference<>(){});
        } catch (IOException | ExecutionException | InterruptedException e) {
            log.error("Error while retrieving GitHub emails", e);
            throw new OauthFlowException();
        }
    }
}
