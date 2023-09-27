package com.github.mkorman9.jwtquarkus.weather;

public record Weather(
    double temperature,
    double windSpeed,
    double windDirection
) {
    public static Weather fromApiResponse(WeatherApiResponse response) {
        return new Weather(
            response.currentWeather().temperature(),
            response.currentWeather().windSpeed(),
            response.currentWeather().windDirection()
        );
    }
}
