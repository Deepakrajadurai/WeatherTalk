package com.example.weathertalk;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

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
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class WeatherDashboardActivity extends AppCompatActivity {

    private LineChart lineChart;
    private BarChart barChart;
    private PieChart pieChart;

    private Button btnLine, btnBar, btnPie;

    // Dynamic forecast data
    private float[] city1Temps = new float[7];
    private float[] city2Temps = new float[7];
    private int[] city1Wind = new int[7];
    private int[] city2Wind = new int[7];

    private final String city1Name = "Berlin";
    private final double city1Lat = 52.52, city1Lon = 13.41;
    private final String city2Name = "London";
    private final double city2Lat = 51.5072, city2Lon = -0.1276;

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

        // Load real forecast
        new FetchForecastTask(city1Lat, city1Lon, true).execute();
        new FetchForecastTask(city2Lat, city2Lon, false).execute();

        btnLine.setOnClickListener(v -> showChart(lineChart));
        btnBar.setOnClickListener(v -> {
            loadBarData();
            showChart(barChart);
        });
        btnPie.setOnClickListener(v -> {
            loadPieData();
            showChart(pieChart);
        });
    }

    private void showChart(View chartToShow) {
        lineChart.setVisibility(View.GONE);
        barChart.setVisibility(View.GONE);
        pieChart.setVisibility(View.GONE);
        chartToShow.setVisibility(View.VISIBLE);
    }

    // Async task for API
    private class FetchForecastTask extends AsyncTask<Void, Void, String> {
        private final double lat, lon;
        private final boolean isCity1;

        FetchForecastTask(double lat, double lon, boolean isCity1) {
            this.lat = lat;
            this.lon = lon;
            this.isCity1 = isCity1;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                String urlStr = "https://api.open-meteo.com/v1/forecast?latitude=" + lat +
                        "&longitude=" + lon +
                        "&daily=temperature_2m_max,windspeed_10m_max&timezone=auto";
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();
                return response.toString();
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
                    JSONObject daily = obj.getJSONObject("daily");
                    JSONArray temps = daily.getJSONArray("temperature_2m_max");
                    JSONArray winds = daily.getJSONArray("windspeed_10m_max");

                    for (int i = 0; i < 7; i++) {
                        if (isCity1) {
                            city1Temps[i] = (float) temps.getDouble(i);
                            city1Wind[i] = (int) winds.getDouble(i);
                        } else {
                            city2Temps[i] = (float) temps.getDouble(i);
                            city2Wind[i] = (int) winds.getDouble(i);
                        }
                    }
                    loadLineData();
                    enableTapListener();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadLineData() {
        List<Entry> entriesCity1 = new ArrayList<>();
        List<Entry> entriesCity2 = new ArrayList<>();

        for (int i = 0; i < city1Temps.length; i++) {
            entriesCity1.add(new Entry(i, city1Temps[i]));
            entriesCity2.add(new Entry(i, city2Temps[i]));
        }

        LineDataSet dataSet1 = new LineDataSet(entriesCity1, city1Name + " (°C)");
        dataSet1.setColor(Color.BLUE);
        dataSet1.setCircleColor(Color.RED);

        LineDataSet dataSet2 = new LineDataSet(entriesCity2, city2Name + " (°C)");
        dataSet2.setColor(Color.GREEN);
        dataSet2.setCircleColor(Color.MAGENTA);

        lineChart.setData(new LineData(dataSet1, dataSet2));
        lineChart.invalidate();
    }

    private void loadBarData() {
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < city1Temps.length; i++) {
            entries.add(new BarEntry(i, new float[]{city1Temps[i], city2Temps[i]}));
        }
        BarDataSet barDataSet = new BarDataSet(entries, "Temps");
        barDataSet.setColors(new int[]{Color.BLUE, Color.GREEN});
        barDataSet.setStackLabels(new String[]{city1Name, city2Name});
        barChart.setData(new BarData(barDataSet));
        barChart.invalidate();
    }

    private void loadPieData() {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(average(city1Temps), city1Name + " Avg"));
        entries.add(new PieEntry(average(city2Temps), city2Name + " Avg"));
        PieDataSet dataSet = new PieDataSet(entries, "Avg Temps");
        dataSet.setColors(new int[]{Color.BLUE, Color.GREEN});
        pieChart.setData(new PieData(dataSet));
        pieChart.invalidate();
    }

    private float average(float[] values) {
        float sum = 0;
        for (float v : values) sum += v;
        return sum / values.length;
    }

    private void enableTapListener() {
        lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, com.github.mikephil.charting.highlight.Highlight h) {
                int dayIndex = (int) e.getX();
                String dataset = lineChart.getLineData().getDataSetByIndex(h.getDataSetIndex()).getLabel();
                int wind;

                if (dataset.contains(city1Name)) {
                    wind = city1Wind[dayIndex];
                } else {
                    wind = city2Wind[dayIndex];
                }

                new AlertDialog.Builder(WeatherDashboardActivity.this)
                        .setTitle("Day " + (dayIndex + 1))
                        .setMessage(dataset +
                                "\nTemp: " + e.getY() + "°C" +
                                "\nWind: " + wind + " km/h")
                        .setPositiveButton("OK", null)
                        .show();
            }

            @Override
            public void onNothingSelected() {}
        });
    }
}
