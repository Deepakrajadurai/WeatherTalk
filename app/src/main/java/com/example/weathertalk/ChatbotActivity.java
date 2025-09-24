package com.example.weathertalk;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class ChatbotActivity extends AppCompatActivity {
    private EditText input;
    private TextView chat;
    private Button send, back;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        // Allow network on main thread for simple demo (NOT recommended for production)
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        input = findViewById(R.id.editChat);
        chat = findViewById(R.id.txtChat);
        send = findViewById(R.id.btnSend);
        back = findViewById(R.id.btnBack);
        scrollView = findViewById(R.id.scrollChat);

        chat.setText("Chat starts here...\n\n");

        send.setOnClickListener(v -> {
            String userMsg = input.getText().toString().trim();
            if (userMsg.isEmpty()) return;

            chat.append("🧑 You: " + userMsg + "\n");
            String botReply = getBotReply(userMsg);
            chat.append("🤖 Bot: " + botReply + "\n\n");

            input.setText("");
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
        });

        back.setOnClickListener(v -> finish());
    }

    // Main chatbot logic
    private String getBotReply(String msg) {
        if (msg == null) return "Say something!";

        String lower = msg.toLowerCase(Locale.ROOT).trim();

        if (lower.contains("hello") || lower.contains("hi") || lower.contains("hey")) {
            return "Hello there! 👋 I’m WeatherBot, your friendly sky guide. Ask me about the weather!";
        } else if (lower.contains("bye")) {
            return "Goodbye! 👋 Stay safe and check the weather often!";
        } else if (lower.contains("thank")) {
            return "You’re welcome! 😊";
        } else if (lower.contains("your name")) {
            return "I’m WeatherBot 🤖 — your weather assistant.";
        } else if (lower.contains("forecast")) {
            String city = extractCity(lower);
            if (city != null) return fetchForecast(city);
            return "Try: 'forecast in London'";
        } else if (lower.contains("weather")) {
            if (!isInternetAvailable()) return "⚠️ No internet connection right now.";
            String city = extractCity(lower);
            if (city != null) return fetchLiveWeather(city);
            return "Try: 'weather in Berlin'";
        }

        // fallback
        return "I’m still learning! Try asking:\n - 'weather in London'\n - 'forecast in Paris'\n - 'how is the weather today in Berlin'";
    }

    // Fetch current weather for a city (uses Open-Meteo geocoding + current_weather)
    private String fetchLiveWeather(String city) {
        try {
            double[] latlon = geocodeCity(city);
            if (latlon == null) return "🤔 I couldn't find *" + city + "*.";

            double lat = latlon[0], lon = latlon[1];
            String weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=" + lat +
                    "&longitude=" + lon + "&current_weather=true";

            HttpURLConnection conn = (HttpURLConnection) new URL(weatherUrl).openConnection();
            conn.setRequestMethod("GET");
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder(); String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();

            JSONObject obj = new JSONObject(sb.toString());
            if (!obj.has("current_weather")) return "No current weather data available.";

            JSONObject current = obj.getJSONObject("current_weather");
            String temp = current.optString("temperature", "N/A");
            String wind = current.optString("windspeed", "N/A");
            int code = current.optInt("weathercode", -1);
            String condition = mapWeatherCode(code);

            return "🌍 Weather in " + capitalize(city) + ":\n" +
                    "🌡️ Temp: " + temp + "°C\n" +
                    "💨 Wind: " + wind + " km/h\n" +
                    "☁️ " + condition;
        } catch (Exception e) {
            e.printStackTrace();
            return "⚠️ Error fetching weather: " + e.getMessage();
        }
    }

    // Fetch 7-day forecast (daily min/max)
    private String fetchForecast(String city) {
        try {
            double[] latlon = geocodeCity(city);
            if (latlon == null) return "🤔 I couldn't find *" + city + "* for forecast.";

            double lat = latlon[0], lon = latlon[1];
            String url = "https://api.open-meteo.com/v1/forecast?latitude=" + lat +
                    "&longitude=" + lon + "&daily=temperature_2m_max,temperature_2m_min&timezone=auto";

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder(); String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();

            JSONObject obj = new JSONObject(sb.toString());
            JSONObject daily = obj.getJSONObject("daily");
            JSONArray dates = daily.getJSONArray("time");
            JSONArray tmax = daily.getJSONArray("temperature_2m_max");
            JSONArray tmin = daily.getJSONArray("temperature_2m_min");

            StringBuilder res = new StringBuilder("📅 7-day forecast for " + capitalize(city) + ":\n");
            // limit length to what API returns (can be 7+), iterate through returned length
            int n = Math.min(dates.length(), Math.min(tmax.length(), tmin.length()));
            for (int i = 0; i < n; i++) {
                res.append(dates.getString(i))
                        .append(": ")
                        .append(tmin.getDouble(i))
                        .append("°C - ")
                        .append(tmax.getDouble(i))
                        .append("°C\n");
            }
            return res.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "⚠️ Error fetching forecast: " + e.getMessage();
        }
    }

    // Geocode: return [lat, lon] or null
    private double[] geocodeCity(String city) {
        try {
            String geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=" +
                    city.replaceAll(" ", "%20") + "&count=1";
            HttpURLConnection conn = (HttpURLConnection) new URL(geoUrl).openConnection();
            conn.setRequestMethod("GET");
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder(); String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();

            JSONObject obj = new JSONObject(sb.toString());
            if (!obj.has("results")) return null;
            JSONArray results = obj.getJSONArray("results");
            if (results.length() == 0) return null;
            JSONObject first = results.getJSONObject(0);
            double lat = first.getDouble("latitude");
            double lon = first.getDouble("longitude");
            return new double[]{lat, lon};
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Map Open-Meteo weather codes to readable text
    private String mapWeatherCode(int code) {
        switch (code) {
            case 0: return "Clear sky ☀️";
            case 1: case 2: case 3: return "Mainly clear / partly cloudy ⛅";
            case 45: case 48: return "Fog 🌫️";
            case 51: case 53: case 55: return "Drizzle 🌦️";
            case 61: case 63: case 65: return "Rain 🌧️";
            case 71: case 73: case 75: return "Snow ❄️";
            case 95: return "Thunderstorm ⛈️";
            default: return "Condition code: " + code;
        }
    }

    // Heuristic extractor for city from phrases
    private String extractCity(String msg) {
        if (msg == null) return null;
        String s = msg.toLowerCase(Locale.ROOT);

        // patterns: "weather in X", "forecast in X", "in X"
        if (s.contains("weather in")) {
            return cleanupCity(s.substring(s.indexOf("weather in") + 10));
        } else if (s.contains("forecast in")) {
            return cleanupCity(s.substring(s.indexOf("forecast in") + 11));
        } else if (s.contains(" in ")) {
            return cleanupCity(s.substring(s.indexOf(" in ") + 4));
        }
        return null;
    }

    private String cleanupCity(String raw) {
        if (raw == null) return null;
        String city = raw.replaceAll("(today|now|please|the|city)", "")
                .replaceAll("[?.!,]", "")
                .trim();
        return city.isEmpty() ? null : city;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0,1).toUpperCase(Locale.ROOT) + s.substring(1);
    }

    // Simple connectivity check
    private boolean isInternetAvailable() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo active = cm.getActiveNetworkInfo();
                return active != null && active.isConnected();
            }
        } catch (Exception ignored) {}
        return false;
    }
}
