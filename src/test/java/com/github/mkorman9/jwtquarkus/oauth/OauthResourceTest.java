package com.github.mkorman9.jwtquarkus.oauth;

import com.github.mkorman9.jwtquarkus.account.AccountResource;
import com.github.mkorman9.jwtquarkus.oauth.github.GithubAPI;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.apache.http.client.utils.URLEncodedUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.URI;
import java.nio.charset.Charset;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class OauthResourceTest {
    @InjectMock
    GithubAPI githubAPI;

    @Inject
    OauthStateService oauthStateService;

    @Test
    public void testLogin() {
        Mockito.when(githubAPI.getLoginUrl(Mockito.anyString())).thenAnswer(invocation -> {
            var state = (String) invocation.getArgument(0);
            return URI.create("https://example.com/?state=" + state);
        });

        var redirectResponse = given()
            .redirects().follow(false)
            .when().get("/oauth/login")
            .then()
            .statusCode(303);

        var location = URI.create(redirectResponse.extract().header("Location"));
        var cookie = redirectResponse.extract().cookie("oauth2_cookie");
        var state = URLEncodedUtils.parse(location, Charset.defaultCharset()).get(0).getValue();

        var isValid = oauthStateService.validateState(state, cookie).valid();
        assertThat(isValid).isTrue();
    }

    @Test
    public void testConnectAccount() {
        Mockito.when(githubAPI.getConnectAccountUrl(Mockito.anyString())).thenAnswer(invocation -> {
            var state = (String) invocation.getArgument(0);
            return URI.create("https://example.com/?state=" + state);
        });

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

        var location = URI.create(redirectResponse.extract().header("Location"));
        var cookie = redirectResponse.extract().cookie("oauth2_cookie");
        var state = URLEncodedUtils.parse(location, Charset.defaultCharset()).get(0).getValue();

        var stateValidation = oauthStateService.validateState(state, cookie);
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
}
