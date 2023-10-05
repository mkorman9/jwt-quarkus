package com.github.mkorman9.jwtquarkus.helloworld;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class HelloWorldResourceTest {
    private static final String SUBJECT = "test";

    @Test
    public void testHelloWorldAnonymousAccess() {
        given()
            .when().get("/hello")
            .then()
            .statusCode(401);
    }

    @Test
    @TestSecurity(user = SUBJECT)
    public void testHelloWorld() {
        given()
            .when().get("/hello")
            .then()
            .statusCode(200)
            .body(is("Hello " + SUBJECT));
    }
}
