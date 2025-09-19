package com.example.weathertalk;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    EditText editCityInput;
    Button btnAddCity;
    LineChart lineChart;

    List<String> cities = new ArrayList<>();
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_dashboard);

        editCityInput = findViewById(R.id.editCityInput);
        btnAddCity = findViewById(R.id.btnAddCity);
        lineChart = findViewById(R.id.lineChart);
    }
}
