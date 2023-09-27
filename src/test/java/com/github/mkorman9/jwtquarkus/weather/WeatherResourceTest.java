package com.github.mkorman9.jwtquarkus.weather;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.config.JsonConfig;
import io.restassured.path.json.config.JsonPathConfig;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class WeatherResourceTest {
    @InjectMock
    @RestClient
    WeatherClient weatherClient;

    @Test
    public void testGetWeather() {
        var temperature = 21.0;
        var windSpeed = 4.0;
        var windDirection = 90.0;
        Mockito.when(weatherClient.getWeather(Mockito.any(), Mockito.any())).thenReturn(new WeatherApiResponse(
            21.0,
            37.0,
            40.0,
            new WeatherApiResponse.CurrentWeather(
                temperature,
                windSpeed,
                windDirection,
                0,
                0,
                "2023-09-27T23:25"
            )
        ));

        given()
            .config(RestAssured.config().jsonConfig(
                JsonConfig.jsonConfig().numberReturnType(JsonPathConfig.NumberReturnType.DOUBLE)
            ))
            .when().get("/weather")
            .then()
            .statusCode(200)
            .body("temperature", is(temperature))
            .body("windSpeed", is(windSpeed))
            .body("windDirection", is(windDirection));
    }
}
