package com.example.weathertalk;

import android.os.StrictMode;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherApiClient {

    // Step 1: Get lat/lon for city
    public static double[] getCoordsFromCity(String city) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            String apiUrl = "https://nominatim.openstreetmap.org/search?q="
                    + city + "&format=json&limit=1";
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "WeatherTalkApp/1.0"); // Nominatim requires UA

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            reader.close();

            JSONArray arr = new JSONArray(response.toString());
            if (arr.length() > 0) {
                JSONObject obj = arr.getJSONObject(0);
                double lat = obj.getDouble("lat");
                double lon = obj.getDouble("lon");
                return new double[]{lat, lon};
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // if lookup fails
    }

    // Step 2: Get weather for lat/lon
    public static String getWeather(double lat, double lon) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            String apiUrl = "https://api.open-meteo.com/v1/forecast?latitude="
                    + lat + "&longitude=" + lon + "&current_weather=true";
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            reader.close();

            JSONObject json = new JSONObject(response.toString());
            JSONObject current = json.getJSONObject("current_weather");
            double temp = current.getDouble("temperature");
            double wind = current.getDouble("windspeed");
            int code = current.getInt("weathercode");

            String condition = mapWeatherCode(code);

            return "Temp: " + temp + " °C\nCondition: " + condition + "\nWind: " + wind + " km/h";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error fetching weather: " + e.getMessage();
        }
    }

    // Step 3: Convert Open-Meteo codes to text
    private static String mapWeatherCode(int code) {
        switch (code) {
            case 0: return "Clear sky";
            case 1: case 2: case 3: return "Partly cloudy";
            case 45: case 48: return "Fog";
            case 51: case 53: case 55: return "Drizzle";
            case 61: case 63: case 65: return "Rain";
            case 71: case 73: case 75: return "Snow";
            case 95: return "Thunderstorm";
            default: return "Unknown";
        }
    }
}
