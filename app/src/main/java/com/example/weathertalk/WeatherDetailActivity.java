package com.example.weathertalk;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_detail);

        String temp = getIntent().getStringExtra("temp");
        String condition = getIntent().getStringExtra("condition");

        TextView txtDetail = findViewById(R.id.txtDetail);
        txtDetail.setText("Temperature: " + temp + " °C\nCondition: " + condition);

        LinearLayout container = findViewById(R.id.weatherContainer);
        CustomWeatherView customView = new CustomWeatherView(this, Float.parseFloat(temp));
        container.addView(customView);
    }
}
