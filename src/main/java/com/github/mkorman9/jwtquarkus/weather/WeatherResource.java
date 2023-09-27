package com.github.mkorman9.jwtquarkus.weather;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/weather")
@Produces(MediaType.APPLICATION_JSON)
public class WeatherResource {
    @RestClient
    WeatherClient weatherClient;

    @GET
    public Weather getWeather() {
        return Weather.fromApiResponse(
            weatherClient.getWeather("52.52", "13.419998")
        );
    }
}
