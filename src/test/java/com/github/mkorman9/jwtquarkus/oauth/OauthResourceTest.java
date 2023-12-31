package com.github.mkorman9.jwtquarkus.oauth;

import com.github.mkorman9.jwtquarkus.account.AccountResource;
import com.github.mkorman9.jwtquarkus.account.AccountService;
import com.github.mkorman9.jwtquarkus.oauth.github.GithubAPI;
import com.github.mkorman9.jwtquarkus.oauth.github.GithubUserInfo;
import com.github.scribejava.core.model.OAuth2AccessToken;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ValidatableResponse;
import jakarta.inject.Inject;
import org.apache.http.client.utils.URLEncodedUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class OauthResourceTest {
    @InjectMock
    GithubAPI githubAPI;

    @Inject
    OauthStateService oauthStateService;

    @Inject
    AccountService accountService;

    @Test
    public void testLogin() {
        Mockito.when(githubAPI.getLoginUrl(Mockito.anyString())).thenAnswer(mockRedirectUrl());

        var redirectResponse = given()
            .redirects().follow(false)
            .when().get("/oauth/login")
            .then()
            .statusCode(303);
        var redirectParams = extractRedirectParams(redirectResponse);

        var isValid = oauthStateService.validateState(redirectParams.state(), redirectParams.cookie()).valid();
        assertThat(isValid).isTrue();
    }

    @Test
    public void testLoginCallback() {
        final var code = "987654321";
        final var githubAccessToken = new OAuth2AccessToken("access_token");
        final var githubUserInfo = GithubUserInfo.builder()
            .id(54321)
            .name("Test User")
            .email("user@example.com")
            .build();

        Mockito.when(githubAPI.getLoginUrl(Mockito.anyString())).thenAnswer(mockRedirectUrl());
        Mockito.when(githubAPI.retrieveAccessToken(Mockito.eq(code))).thenReturn(githubAccessToken);
        Mockito.when(githubAPI.retrieveUserInfo(Mockito.eq(githubAccessToken))).thenReturn(githubUserInfo);

        var newAccountResponse = given()
            .when().get("/account/new")
            .then()
            .statusCode(200)
            .extract().body().as(AccountResource.AccountResponse.class);
        accountService.connectGithubAccount(githubUserInfo, UUID.fromString(newAccountResponse.id()));

        var redirectResponse = given()
            .redirects().follow(false)
            .when().get("/oauth/login")
            .then()
            .statusCode(303);
        var redirectParams = extractRedirectParams(redirectResponse);

        given()
            .when()
            .queryParam("code", code)
            .queryParam("state", redirectParams.state())
            .cookie("oauth2_cookie", redirectParams.cookie())
            .get("/oauth/callback/login")
            .then()
            .statusCode(200);
    }

    @Test
    public void testLoginCallbackInvalidParams() {
        given()
            .when()
            .queryParam("code", "invalid_code")
            .queryParam("state", "invalid_state")
            .cookie("oauth2_cookie", "invalid_cookie")
            .get("/oauth/callback/login")
            .then()
            .statusCode(401);
    }

    @Test
    public void testConnectAccount() {
        Mockito.when(githubAPI.getConnectAccountUrl(Mockito.anyString())).thenAnswer(mockRedirectUrl());

        var newAccountResponse = given()
            .when().get("/account/new")
            .then()
            .statusCode(200)
            .extract().body().as(AccountResource.AccountResponse.class);

        var redirectResponse = given()
            .redirects().follow(false)
            .when()
            .queryParam("accessToken", newAccountResponse.token().accessToken())
            .get("/oauth/connect-account")
            .then()
            .statusCode(303);
        var redirectParams = extractRedirectParams(redirectResponse);

        var stateValidation = oauthStateService.validateState(redirectParams.state(), redirectParams.cookie());
        assertThat(stateValidation.valid()).isTrue();
        assertThat(stateValidation.subject()).isEqualTo(newAccountResponse.id());
    }

    @Test
    public void testConnectAccountInvalidToken() {
        given()
            .redirects().follow(false)
            .when()
            .queryParam("accessToken", "INVALID_TOKEN")
            .get("/oauth/connect-account")
            .then()
            .statusCode(401);
    }

    @Test
    public void testConnectAccountCallback() {
        final var code = "123456789";
        final var githubAccessToken = new OAuth2AccessToken("access_token");

        Mockito.when(githubAPI.getConnectAccountUrl(Mockito.anyString())).thenAnswer(mockRedirectUrl());
        Mockito.when(githubAPI.retrieveAccessToken(Mockito.eq(code))).thenReturn(githubAccessToken);
        Mockito.when(githubAPI.retrieveUserInfo(Mockito.eq(githubAccessToken))).thenReturn(
            GithubUserInfo.builder()
                .id(12345)
                .name("Test User")
                .email("user@example.com")
                .build()
        );

        var newAccountResponse = given()
            .when().get("/account/new")
            .then()
            .statusCode(200)
            .extract().body().as(AccountResource.AccountResponse.class);

        var redirectResponse = given()
            .redirects().follow(false)
            .when()
            .queryParam("accessToken", newAccountResponse.token().accessToken())
            .get("/oauth/connect-account")
            .then()
            .statusCode(303);
        var redirectParams = extractRedirectParams(redirectResponse);

        given()
            .when()
            .queryParam("code", code)
            .queryParam("state", redirectParams.state())
            .cookie("oauth2_cookie", redirectParams.cookie())
            .get("/oauth/callback/connect-account")
            .then()
            .statusCode(200);
    }

    private Answer<?> mockRedirectUrl() {
        return invocation -> {
            var state = (String) invocation.getArgument(0);
            return URI.create("https://example.com/?state=" + state);
        };
    }

    private RedirectParams extractRedirectParams(ValidatableResponse response) {
        var location = URI.create(response.extract().header("Location"));
        return new RedirectParams(
            response.extract().cookie("oauth2_cookie"),
            URLEncodedUtils.parse(location, Charset.defaultCharset()).get(0).getValue()
        );
    }

    private record RedirectParams(
        String cookie,
        String state
    ) {
    }
}
