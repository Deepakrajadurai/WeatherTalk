package com.example.weathertalk;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class WeatherDashboardActivity extends AppCompatActivity {

    private LineChart lineChart;
    private BarChart barChart;
    private PieChart pieChart;

    private Button btnLine, btnBar, btnPie, btnAddCity;
    private EditText editCityInput;

    // Hold multiple cities
    private final List<String> cityNames = new ArrayList<>();
    private final List<float[]> cityTemps = new ArrayList<>();
    private final List<int[]> cityWinds = new ArrayList<>();

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

        // Default: Berlin & London
        fetchForecast("Berlin", 52.52, 13.41);
        fetchForecast("London", 51.5072, -0.1276);

        btnLine.setOnClickListener(v -> showChart(lineChart));
        btnBar.setOnClickListener(v -> {
            loadBarData();
            showChart(barChart);
        });
        btnPie.setOnClickListener(v -> {
            loadPieData();
            showChart(pieChart);
        });

        btnAddCity.setOnClickListener(v -> {
            String cityName = editCityInput.getText().toString().trim();
            if (cityName.isEmpty()) {
                Toast.makeText(this, "Please enter a city", Toast.LENGTH_SHORT).show();
                return;
            }
            fetchCityCoordinates(cityName);
        });
    }

    /** 🌍 Step 1: Geocode city -> get lat/lon */
    private void fetchCityCoordinates(String cityName) {
        new AsyncTask<Void, Void, double[]>() {
            @Override
            protected double[] doInBackground(Void... voids) {
                try {
                    String query = URLEncoder.encode(cityName, "UTF-8");
                    String urlStr = "https://geocoding-api.open-meteo.com/v1/search?name=" + query + "&count=1";
                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) response.append(line);
                    reader.close();

                    JSONObject obj = new JSONObject(response.toString());
                    JSONArray results = obj.optJSONArray("results");
                    if (results != null && results.length() > 0) {
                        JSONObject city = results.getJSONObject(0);
                        double lat = city.getDouble("latitude");
                        double lon = city.getDouble("longitude");
                        return new double[]{lat, lon};
                    }
                } catch (Exception e) {
                    Log.e("Geocode", "Error", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(double[] coords) {
                if (coords != null) {
                    fetchForecast(cityName, coords[0], coords[1]);
                } else {
                    Toast.makeText(WeatherDashboardActivity.this, "City not found!", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    /** 🌦️ Step 2: Fetch forecast */
    private void fetchForecast(String cityName, double lat, double lon) {
        new FetchForecastTask(cityName, lat, lon).execute();
    }

    private class FetchForecastTask extends AsyncTask<Void, Void, String> {
        private final String cityName;
        private final double lat, lon;

        FetchForecastTask(String cityName, double lat, double lon) {
            this.cityName = cityName;
            this.lat = lat;
            this.lon = lon;
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
                Log.e("WeatherAPI", "Error fetching forecast", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject obj = new JSONObject(result);
                    JSONObject daily = obj.getJSONObject("daily");
                    JSONArray tempsJson = daily.getJSONArray("temperature_2m_max");
                    JSONArray windsJson = daily.getJSONArray("windspeed_10m_max");

                    float[] temps = new float[7];
                    int[] winds = new int[7];

                    for (int i = 0; i < 7; i++) {
                        temps[i] = (float) tempsJson.getDouble(i);
                        winds[i] = (int) windsJson.getDouble(i);
                    }

                    cityNames.add(cityName);
                    cityTemps.add(temps);
                    cityWinds.add(winds);

                    loadLineData();
                } catch (Exception e) {
                    Log.e("WeatherAPI", "Parse error", e);
                }
            }
        }
    }

    /** 📈 LineChart */
    private void loadLineData() {
        LineData lineData = new LineData();

        for (int c = 0; c < cityNames.size(); c++) {
            String city = cityNames.get(c);
            float[] temps = cityTemps.get(c);

            List<Entry> entries = new ArrayList<>();
            for (int i = 0; i < temps.length; i++) {
                entries.add(new Entry(i, temps[i]));
            }

            LineDataSet dataSet = new LineDataSet(entries, city + " (°C)");
            int color = (int) (Math.random() * 0xFFFFFF) | 0xFF000000;
            dataSet.setColor(color);
            dataSet.setCircleColor(color);
            dataSet.setLineWidth(2f);
            dataSet.setValueTextSize(10f);

            lineData.addDataSet(dataSet);
        }

        lineChart.setData(lineData);
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();

        enableTapListener();
    }

    /** 📊 BarChart */
    private void loadBarData() {
        List<BarEntry> entries = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            float[] values = new float[cityNames.size()];
            for (int c = 0; c < cityTemps.size(); c++) {
                values[c] = cityTemps.get(c)[i];
            }
            entries.add(new BarEntry(i, values));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Temps");
        dataSet.setColors(Color.BLUE, Color.GREEN, Color.RED, Color.MAGENTA, Color.CYAN);
        dataSet.setStackLabels(cityNames.toArray(new String[0]));
        barChart.setData(new BarData(dataSet));
        barChart.invalidate();
    }

    /** 🥧 PieChart */
    private void loadPieData() {
        List<PieEntry> entries = new ArrayList<>();
        for (int c = 0; c < cityNames.size(); c++) {
            entries.add(new PieEntry(average(cityTemps.get(c)), cityNames.get(c)));
        }
        PieDataSet dataSet = new PieDataSet(entries, "Avg Temps");
        dataSet.setColors(Color.BLUE, Color.GREEN, Color.RED, Color.MAGENTA, Color.CYAN);
        pieChart.setData(new PieData(dataSet));
        pieChart.invalidate();
    }

    private float average(float[] values) {
        float sum = 0;
        for (float v : values) sum += v;
        return sum / values.length;
    }

    /** Toggle chart visibility */
    private void showChart(View chartToShow) {
        lineChart.setVisibility(View.GONE);
        barChart.setVisibility(View.GONE);
        pieChart.setVisibility(View.GONE);
        chartToShow.setVisibility(View.VISIBLE);
    }

    /** 📌 Tap listener for details */
    private void enableTapListener() {
        lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, com.github.mikephil.charting.highlight.Highlight h) {
                int dayIndex = (int) e.getX();
                String dataset = lineChart.getLineData().getDataSetByIndex(h.getDataSetIndex()).getLabel();
                int wind = 0;

                String cityName = dataset.split(" ")[0];
                int idx = cityNames.indexOf(cityName);
                if (idx >= 0) {
                    wind = cityWinds.get(idx)[dayIndex];
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
