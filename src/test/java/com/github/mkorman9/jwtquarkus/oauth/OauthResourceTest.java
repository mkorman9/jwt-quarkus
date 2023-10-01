package com.github.mkorman9.jwtquarkus.oauth;

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

        oauthStateService.validateState(state, cookie);
    }
}
