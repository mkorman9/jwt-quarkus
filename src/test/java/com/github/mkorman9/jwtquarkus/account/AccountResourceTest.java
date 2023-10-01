package com.github.mkorman9.jwtquarkus.account;

import com.github.mkorman9.jwtquarkus.token.TokenResponse;
import com.github.mkorman9.jwtquarkus.token.TokenService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class AccountResourceTest {
    @Inject
    TokenService tokenService;

    @Test
    public void testNewAccount() {
        var response = given()
            .when().get("/account/new")
            .then()
            .statusCode(200)
            .extract().body().as(AccountResource.AccountResponse.class);

        var accountId = response.id();
        var accessToken = tokenService.validateAccessToken(response.token().accessToken());
        assertThat(accessToken.subject().toString()).isEqualTo(accountId);
    }

    @Test
    public void testRefreshToken() {
        var newAccountResponse = given()
            .when().get("/account/new")
            .then()
            .statusCode(200)
            .extract().body().as(AccountResource.AccountResponse.class);
        var refreshTokenResponse = given()
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .body(new AccountResource.TokenRefreshPayload(
                newAccountResponse.token().accessToken(),
                newAccountResponse.token().refreshToken()
            ))
            .put("/account/token/refresh")
            .then()
            .statusCode(200)
            .extract().body().as(TokenResponse.class);

        var accountId = newAccountResponse.id();
        var newAccessToken = tokenService.validateAccessToken(refreshTokenResponse.accessToken());
        assertThat(newAccessToken.subject().toString()).isEqualTo(accountId);
    }
}
