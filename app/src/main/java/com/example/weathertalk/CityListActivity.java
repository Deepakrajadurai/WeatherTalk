package com.example.weathertalk;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CityListActivity extends AppCompatActivity {

    RecyclerView recyclerCities;
    CityAdapter adapter;
    List<CityWeather> cityList;
    EditText editNewCity;
    Button btnAddCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_list);

        recyclerCities = findViewById(R.id.recyclerCities);
        editNewCity = findViewById(R.id.editNewCity);
        btnAddCity = findViewById(R.id.btnAddCity);

        recyclerCities.setLayoutManager(new LinearLayoutManager(this));

        cityList = new ArrayList<>();
        // Preloaded cities
        String[] cities = {"London", "New York", "Paris"};
        for (String city : cities) {
            addCityToList(city);
        }

        adapter = new CityAdapter(cityList, position -> {
            cityList.remove(position);
            adapter.notifyItemRemoved(position);
            Toast.makeText(this, "City removed", Toast.LENGTH_SHORT).show();
        });
        recyclerCities.setAdapter(adapter);

        btnAddCity.setOnClickListener(v -> {
            String city = editNewCity.getText().toString().trim();
            if (city.isEmpty()) {
                Toast.makeText(this, "Enter a city", Toast.LENGTH_SHORT).show();
                return;
            }
            addCityToList(city);
            adapter.notifyItemInserted(cityList.size() - 1);
            recyclerCities.scrollToPosition(cityList.size() - 1);
            editNewCity.setText("");
        });
    }

    private void addCityToList(String city) {
        double[] coords = WeatherApiClient.getCoordsFromCity(city);
        String weather = "Error fetching";
        if (coords != null) {
            weather = WeatherApiClient.getWeather(coords[0], coords[1]);
        }
        cityList.add(new CityWeather(city, weather));
    }
}
