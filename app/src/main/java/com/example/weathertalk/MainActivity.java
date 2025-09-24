package com.example.weathertalk;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText editCity;
    private TextView txtWeather;
    private Button btnGetWeather, btnDetails, btnOpenChat, btnSettings, btnCityList, btnDashboard, btnAiAssistant;

    private FusedLocationProviderClient fusedLocationClient;

    private String lastTemp = "";
    private String lastCondition = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editCity = findViewById(R.id.editCity);
        txtWeather = findViewById(R.id.txtWeather);
        btnGetWeather = findViewById(R.id.btnGetWeather);
        btnDetails = findViewById(R.id.btnDetails);
        btnOpenChat = findViewById(R.id.btnOpenChat);
        btnSettings = findViewById(R.id.btnSettings);
        btnCityList = findViewById(R.id.btnCityList);
        btnDashboard = findViewById(R.id.btnDashboard);
        btnAiAssistant = findViewById(R.id.btnAiAssistant);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Get weather by city
        btnGetWeather.setOnClickListener(v -> {
            String city = editCity.getText().toString().trim();
            if (!city.isEmpty()) {
                new FetchWeatherTask(city).execute();
            } else {
                Toast.makeText(this, "Enter a city name", Toast.LENGTH_SHORT).show();
            }
        });

        // Go to details
        btnDetails.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WeatherDetailActivity.class);
            intent.putExtra("temp", lastTemp);
            intent.putExtra("condition", lastCondition);
            startActivity(intent);
        });

        // Chatbot
        btnOpenChat.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChatbotActivity.class);
            startActivity(intent);
        });

        // Settings
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // Multiple cities
        btnCityList.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CityListActivity.class);
            startActivity(intent);
        });

        // Dashboard
        btnDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WeatherDashboardActivity.class);
            startActivity(intent);
        });

        // 🤖 AI Assistant (uses GPS)
        btnAiAssistant.setOnClickListener(v -> checkLocationPermission());
    }

    // --- Location ---
    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            getUserLocation();
        }
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                fetchWeatherForLocation(location);
            } else {
                Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchWeatherForLocation(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        new FetchWeatherTask(lat, lon).execute();
    }

    // --- Weather Fetch ---
    private class FetchWeatherTask extends AsyncTask<Void, Void, String> {
        private String cityName;
        private Double lat, lon;

        // City-based constructor
        FetchWeatherTask(String cityName) {
            this.cityName = cityName;
        }

        // Lat/Lon constructor
        FetchWeatherTask(Double lat, Double lon) {
            this.lat = lat;
            this.lon = lon;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                String urlStr;
                if (cityName != null) {
                    urlStr = "https://geocoding-api.open-meteo.com/v1/search?name=" + cityName + "&count=1";
                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    reader.close();

                    JSONObject obj = new JSONObject(sb.toString());
                    JSONObject first = obj.getJSONArray("results").getJSONObject(0);
                    lat = first.getDouble("latitude");
                    lon = first.getDouble("longitude");
                }

                // Weather API call
                urlStr = "https://api.open-meteo.com/v1/forecast?latitude=" + lat +
                        "&longitude=" + lon +
                        "&current_weather=true";
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();
                return sb.toString();

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject obj = new JSONObject(result);
                    JSONObject current = obj.getJSONObject("current_weather");
                    String temp = current.getString("temperature");
                    String wind = current.getString("windspeed");

                    lastTemp = temp;
                    lastCondition = "Wind: " + wind + " km/h";

                    txtWeather.setText("Temp: " + temp + "°C\nWind: " + wind + " km/h");

                    // 🤖 AI Assistant logic: suggest activity
                    if (lat != null && lon != null && cityName == null) {
                        String suggestion;
                        double t = Double.parseDouble(temp);
                        double w = Double.parseDouble(wind);
                        if (t >= 15 && t <= 25 && w < 10) {
                            suggestion = "Perfect for camping ⛺ or trucking 🚚!";
                        } else if (t > 30) {
                            suggestion = "Too hot ☀️ — better to stay indoors.";
                        } else if (w > 20) {
                            suggestion = "Windy 🌬️ — not great for outdoor activities.";
                        } else {
                            suggestion = "Good weather for a walk 🚶.";
                        }

                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("AI Assistant")
                                .setMessage(suggestion)
                                .setPositiveButton("OK", null)
                                .show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
