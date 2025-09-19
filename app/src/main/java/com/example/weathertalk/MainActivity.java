package com.example.weathertalk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    EditText editCity;
    TextView txtWeather;
    Button btnWeather, btnDetails, btnOpenChat, btnSettings, btnCityList;

    String lastTemp = "";
    String lastCondition = "";

    // GPS
    private static final int LOCATION_REQUEST_CODE = 1001;
    FusedLocationProviderClient fusedLocationClient;

    // Accelerometer
    SensorManager sensorManager;
    private long lastShakeTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editCity = findViewById(R.id.editCity);
        txtWeather = findViewById(R.id.txtWeather);
        btnWeather = findViewById(R.id.btnGetWeather);
        btnDetails = findViewById(R.id.btnDetails);
        btnOpenChat = findViewById(R.id.btnOpenChat);
        btnSettings = findViewById(R.id.btnSettings);
        btnCityList = findViewById(R.id.btnCityList);

        // Init GPS
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE);
        } else {
            fetchLocationWeather();
        }

        // Init Accelerometer
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) {
            Toast.makeText(this, "No accelerometer found!", Toast.LENGTH_SHORT).show();
        }

        // Fetch weather manually
        btnWeather.setOnClickListener(v -> {
            String city = editCity.getText().toString().trim();

            if (city.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter a city", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isInternetAvailable()) {
                Toast.makeText(MainActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                return;
            }

            double[] coords = WeatherApiClient.getCoordsFromCity(city);

            if (coords != null) {
                String weather = WeatherApiClient.getWeather(coords[0], coords[1]);
                updateWeatherDisplay(weather);
            } else {
                txtWeather.setText("Could not find city: " + city);
            }
        });

        // Go to details
        btnDetails.setOnClickListener(v -> {
            if (lastTemp.isEmpty() || lastCondition.isEmpty()) {
                Toast.makeText(MainActivity.this, "Fetch weather first!", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(MainActivity.this, WeatherDetailActivity.class);
            intent.putExtra("temp", lastTemp);
            intent.putExtra("condition", lastCondition);
            startActivity(intent);
        });

        // Go to chatbot
        btnOpenChat.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChatbotActivity.class);
            startActivity(intent);
        });

        // Open settings
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // Open multiple cities (RecyclerView)
        btnCityList.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CityListActivity.class));
        });
    }

    // GPS: handle permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocationWeather();
            } else {
                txtWeather.setText("Location permission denied. Please enter a city manually.");
            }
        }
    }

    // GPS: fetch location weather
    private void fetchLocationWeather() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();

                String weather = WeatherApiClient.getWeather(lat, lon);
                txtWeather.setText("Current Location\n" + weather);
                updateWeatherDisplay(weather);
            } else {
                txtWeather.setText("Unable to detect location.");
            }
        });
    }

    // Accelerometer: detect shake
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            double acceleration = Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH;

            // 🔧 Load threshold from settings
            SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
            int shakeThreshold = prefs.getInt("shake_threshold", 5);

            long currentTime = System.currentTimeMillis();
            if (acceleration > shakeThreshold) {
                if (currentTime - lastShakeTime > 2000) {
                    lastShakeTime = currentTime;
                    Toast.makeText(this, "Shake detected! Refreshing weather...", Toast.LENGTH_SHORT).show();
                    refreshWeather();
                }
            }
        }
    }

    private void refreshWeather() {
        String city = editCity.getText().toString().trim();
        if (!city.isEmpty()) {
            double[] coords = WeatherApiClient.getCoordsFromCity(city);
            if (coords != null) {
                String weather = WeatherApiClient.getWeather(coords[0], coords[1]);
                updateWeatherDisplay(weather);
            }
        } else {
            fetchLocationWeather(); // fallback
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null) {
            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    // Helper: update UI
    private void updateWeatherDisplay(String weather) {
        txtWeather.setText(weather);

        if (weather.contains("Temp:")) {
            try {
                String[] parts = weather.split("\n");
                lastTemp = parts[0].replace("Temp:", "").replace("°C", "").trim();
                lastCondition = parts[1].replace("Condition:", "").trim();
            } catch (Exception e) {
                lastTemp = "";
                lastCondition = "";
            }
        }
    }

    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }
}
