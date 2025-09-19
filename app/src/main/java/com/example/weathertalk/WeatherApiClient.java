package com.example.weathertalk;

import android.util.Log;

public class WeatherApiClient {

    // Example: hardcoded mock for now, later you can fetch from API
    public static double[] getCoordsFromCity(String city) {
        city = city.toLowerCase();

        switch (city) {
            case "london":
                return new double[]{51.5074, -0.1278};
            case "new york":
                return new double[]{40.7128, -74.0060};
            case "paris":
                return new double[]{48.8566, 2.3522};
            case "tokyo":
                return new double[]{35.6762, 139.6503};
            default:
                Log.e("WeatherApiClient", "City not found: " + city);
                return null; // means "unknown city"
        }
    }

    // Example placeholder
    public static String getWeather(double lat, double lon) {
        // Later: call real API (OpenWeather, etc.)
        return "Temp: 22°C\nCondition: Sunny\nWind: 10 km/h";
    }
}
