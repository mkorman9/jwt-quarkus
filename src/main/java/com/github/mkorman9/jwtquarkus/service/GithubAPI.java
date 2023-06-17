package com.github.mkorman9.jwtquarkus.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mkorman9.jwtquarkus.dto.GithubUserEmailResponse;
import com.github.mkorman9.jwtquarkus.dto.GithubUserInfo;
import com.github.mkorman9.jwtquarkus.dto.GithubUserInfoResponse;
import com.github.mkorman9.jwtquarkus.exception.OauthFlowException;
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

    private final OAuth20Service service;

    @Inject
    public GithubAPI(
            @ConfigProperty(name="oauth2.clientId") String clientId,
            @ConfigProperty(name="oauth2.clientSecret") String clientSecret,
            @ConfigProperty(name="oauth2.redirectUrl") String redirectUrl
    ) {
        this.service = new ServiceBuilder(clientId)
                .apiSecret(clientSecret)
                .callback(redirectUrl)
                .build(GitHubApi.instance());
    }

    public String getAuthorizationUrl(String state) {
        return service.createAuthorizationUrlBuilder()
                .state(state)
                .scope(EMAIL_SCOPE)
                .build();
    }

    @SneakyThrows
    public OAuth2AccessToken retrieveAccessToken(String code) {
        try {
            return service.getAccessToken(code);
        } catch (OAuthException e) {
            log.error("Error while retrieving GitHub access token", e);
            throw new OauthFlowException();
        }
    }

    public GithubUserInfo retrieveUserInfo(OAuth2AccessToken accessToken) {
        var userInfo = retrieveUserInfoResponse(accessToken);
        var userEmails = retrieveUserEmailResponses(accessToken);
        return GithubUserInfo.builder()
                .id(userInfo.getId())
                .login(userInfo.getLogin())
                .name(userInfo.getName())
                .email(selectUserEmail(userEmails))
                .avatarUrl(userInfo.getAvatarUrl())
                .build();
    }

    private static String selectUserEmail(List<GithubUserEmailResponse> userEmails) {
        String candidate = "";
        for (var email : userEmails) {
            if (email.isPrimary() && email.isVerified()) {
                return email.getEmail();  // return primary, verified email immediately
            }

            if (email.isVerified()) {
                candidate = email.getEmail();
            }
        }

        return candidate;
    }

    private GithubUserInfoResponse retrieveUserInfoResponse(OAuth2AccessToken accessToken) {
        var request = new OAuthRequest(Verb.GET, USER_INFO_URL);
        service.signRequest(accessToken, request);

        try (var response = service.execute(request)) {
            var body = response.getBody();
            return objectMapper.readValue(body, GithubUserInfoResponse.class);
        } catch (IOException | ExecutionException | InterruptedException e) {
            log.error("Error while retrieving GitHub user info", e);
            throw new OauthFlowException();
        }
    }

    private List<GithubUserEmailResponse> retrieveUserEmailResponses(OAuth2AccessToken accessToken) {
        var request = new OAuthRequest(Verb.GET, USER_EMAIL_URL);
        service.signRequest(accessToken, request);

        try (var response = service.execute(request)) {
            var body = response.getBody();
            return objectMapper.readValue(body, new TypeReference<>(){});
        } catch (IOException | ExecutionException | InterruptedException e) {
            log.error("Error while retrieving GitHub emails", e);
            throw new OauthFlowException();
        }
    }
}
