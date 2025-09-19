package com.example.weathertalk;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class WeatherDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_detail);

        TextView txtTemp = findViewById(R.id.txtTemp);
        TextView txtCondition = findViewById(R.id.txtCondition);
        CustomWeatherView customView = findViewById(R.id.weatherView);

        // Get data from MainActivity
        String tempStr = getIntent().getStringExtra("temp");
        String condition = getIntent().getStringExtra("condition");

        txtTemp.setText("Temperature: " + tempStr + " °C");
        txtCondition.setText("Condition: " + condition);

        // Update thermometer
        try {
            float tempValue = Float.parseFloat(tempStr);
            customView.setTemperature(tempValue);
        } catch (Exception e) {
            customView.setTemperature(0);
        }
    }
}
