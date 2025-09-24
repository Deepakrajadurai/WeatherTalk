package com.example.weathertalk;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CityListActivity extends AppCompatActivity {

    private RecyclerView recyclerCities;
    private CityAdapter cityAdapter;
    private final List<CityWeather> cityList = new ArrayList<>();

    private EditText editNewCity;
    private Button btnAddCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_list);

        recyclerCities = findViewById(R.id.recyclerCities);
        editNewCity = findViewById(R.id.editNewCity);
        btnAddCity = findViewById(R.id.btnAddCity);

        recyclerCities.setLayoutManager(new LinearLayoutManager(this));

        cityAdapter = new CityAdapter(cityList, position -> {
            cityList.remove(position);
            cityAdapter.notifyItemRemoved(position);
            Toast.makeText(this, "City removed", Toast.LENGTH_SHORT).show();
        });

        recyclerCities.setAdapter(cityAdapter);

        btnAddCity.setOnClickListener(v -> {
            String city = editNewCity.getText().toString().trim();
            if (city.isEmpty()) {
                Toast.makeText(this, "Enter a city", Toast.LENGTH_SHORT).show();
                return;
            }
            new FetchWeatherTask(city).execute();
        });
    }

    // ----------------------
    // Fetch weather for city
    // ----------------------
    private class FetchWeatherTask extends AsyncTask<Void, Void, CityWeather> {
        private final String cityName;

        FetchWeatherTask(String cityName) {
            this.cityName = cityName;
        }

        @Override
        protected CityWeather doInBackground(Void... voids) {
            try {
                // Step 1: Geocode city → lat/lon
                String geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=" +
                        cityName + "&count=1";
                URL url = new URL(geoUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONObject geoObj = new JSONObject(sb.toString());
                JSONArray results = geoObj.getJSONArray("results");
                if (results.length() == 0) return null;

                JSONObject city = results.getJSONObject(0);
                double lat = city.getDouble("latitude");
                double lon = city.getDouble("longitude");

                // Step 2: Weather API call
                String weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=" + lat +
                        "&longitude=" + lon + "&current_weather=true";
                URL url2 = new URL(weatherUrl);
                HttpURLConnection conn2 = (HttpURLConnection) url2.openConnection();
                BufferedReader reader2 = new BufferedReader(new InputStreamReader(conn2.getInputStream()));
                StringBuilder sb2 = new StringBuilder();
                String line2;
                while ((line2 = reader2.readLine()) != null) sb2.append(line2);
                reader2.close();

                JSONObject obj = new JSONObject(sb2.toString());
                JSONObject current = obj.getJSONObject("current_weather");

                String temp = current.getString("temperature") + "°C";
                String wind = "Wind: " + current.getString("windspeed") + " km/h";

                return new CityWeather(cityName, temp + ", " + wind);

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(CityWeather result) {
            if (result != null) {
                cityList.add(result);
                cityAdapter.notifyItemInserted(cityList.size() - 1);
                recyclerCities.scrollToPosition(cityList.size() - 1);
                editNewCity.setText("");
            } else {
                Toast.makeText(CityListActivity.this,
                        "City not found or API error!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
