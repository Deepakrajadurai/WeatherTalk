package com.example.weathertalk;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.List;

public class WeatherDashboardActivity extends AppCompatActivity {

    private LineChart lineChart;
    private BarChart barChart;
    private PieChart pieChart;

    private Button btnLine, btnBar, btnPie, btnAddCity;
    private EditText editCityInput;

    private RecyclerView recyclerCities;
    private CityAdapter cityAdapter;
    private final List<String> cityNames = new ArrayList<>();
    private final List<float[]> cityTemps = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_dashboard);

        lineChart = findViewById(R.id.lineChart);
        barChart = findViewById(R.id.barChart);
        pieChart = findViewById(R.id.pieChart);

        btnLine = findViewById(R.id.btnLine);
        btnBar = findViewById(R.id.btnBar);
        btnPie = findViewById(R.id.btnPie);
        btnAddCity = findViewById(R.id.btnAddCity);
        editCityInput = findViewById(R.id.editCityInput);

        recyclerCities = findViewById(R.id.recyclerCities);
        recyclerCities.setLayoutManager(new LinearLayoutManager(this));

        cityAdapter = new CityAdapter(cityNames, position -> {
            cityNames.remove(position);
            cityTemps.remove(position);
            cityAdapter.notifyItemRemoved(position);
            loadLineData();
        });

        recyclerCities.setAdapter(cityAdapter);

        btnLine.setOnClickListener(v -> showChart(lineChart));
        btnBar.setOnClickListener(v -> {
            loadBarData();
            showChart(barChart);
        });
        btnPie.setOnClickListener(v -> {
            loadPieData();
            showChart(pieChart);
        });

        btnAddCity.setOnClickListener(v -> addCity());
    }

    private void addCity() {
        String cityName = editCityInput.getText().toString().trim();
        if (cityName.isEmpty()) {
            Toast.makeText(this, "Enter a city", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mock random data for now
        float[] temps = new float[7];
        for (int i = 0; i < 7; i++) {
            temps[i] = (float) (15 + Math.random() * 10);
        }

        cityNames.add(cityName);
        cityTemps.add(temps);
        cityAdapter.notifyItemInserted(cityNames.size() - 1);

        loadLineData();
        editCityInput.setText("");
    }

    private void showChart(android.view.View chartToShow) {
        lineChart.setVisibility(android.view.View.GONE);
        barChart.setVisibility(android.view.View.GONE);
        pieChart.setVisibility(android.view.View.GONE);
        chartToShow.setVisibility(android.view.View.VISIBLE);
    }

    private void loadLineData() {
        List<LineDataSet> dataSets = new ArrayList<>();
        for (int c = 0; c < cityNames.size(); c++) {
            List<Entry> entries = new ArrayList<>();
            float[] temps = cityTemps.get(c);
            for (int i = 0; i < temps.length; i++) {
                entries.add(new Entry(i, temps[i]));
            }
            LineDataSet dataSet = new LineDataSet(entries, cityNames.get(c));
            dataSet.setColor(Color.rgb((int)(Math.random()*255),
                    (int)(Math.random()*255),
                    (int)(Math.random()*255)));
            dataSet.setCircleRadius(3f);
            dataSets.add(dataSet);
        }
        lineChart.setData(new LineData(dataSets.toArray(new LineDataSet[0])));
        lineChart.invalidate();
    }

    private void loadBarData() {
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            float[] values = new float[cityNames.size()];
            for (int c = 0; c < cityTemps.size(); c++) {
                values[c] = cityTemps.get(c)[i];
            }
            entries.add(new BarEntry(i, values));
        }
        BarDataSet barDataSet = new BarDataSet(entries, "Temps");
        barDataSet.setColors(Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.CYAN);
        barChart.setData(new BarData(barDataSet));
        barChart.invalidate();
    }

    private void loadPieData() {
        List<PieEntry> entries = new ArrayList<>();
        for (int c = 0; c < cityNames.size(); c++) {
            float sum = 0;
            for (float t : cityTemps.get(c)) sum += t;
            entries.add(new PieEntry(sum / 7, cityNames.get(c)));
        }
        PieDataSet dataSet = new PieDataSet(entries, "Avg Temps");
        dataSet.setColors(Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.CYAN);
        pieChart.setData(new PieData(dataSet));
        pieChart.invalidate();
    }
}
