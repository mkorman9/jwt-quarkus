package com.github.mkorman9.jwtquarkus.weather;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WeatherApiResponse(
    double latitude,
    double longitude,
    double elevation,
    @JsonProperty("current_weather") CurrentWeather currentWeather
) {
    public record CurrentWeather(
        double temperature,
        @JsonProperty("windspeed") double windSpeed,
        @JsonProperty("winddirection") double windDirection,
        @JsonProperty("weathercode") int weatherCode,
        @JsonProperty("is_day") int isDay,
        String time
    ) {
    }
}
