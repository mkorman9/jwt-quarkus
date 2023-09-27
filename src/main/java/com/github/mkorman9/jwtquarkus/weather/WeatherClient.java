package com.github.mkorman9.jwtquarkus.weather;

import io.quarkus.rest.client.reactive.ClientQueryParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestQuery;

@RegisterRestClient(baseUri = "https://api.open-meteo.com/v1")
public interface WeatherClient {
    @GET
    @Path("/forecast")
    @ClientQueryParam(name = "current_weather", value = "true")
    WeatherApiResponse getWeather(@RestQuery String latitude, @RestQuery String longitude);
}
