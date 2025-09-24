package com.example.weathertalk;

import android.graphics.Color;
import android.os.AsyncTask;
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
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

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

    private Button btnLine, btnBar, btnPie, btnAddCity;
    private EditText editCityInput;

    private RecyclerView recyclerCities;
    private CityAdapter cityAdapter;
    private final List<CityWeather> cityList = new ArrayList<>();

    private final List<float[]> cityTemps = new ArrayList<>();
    private final List<float[]> cityWinds = new ArrayList<>();
    private final List<int[]> cityCodes = new ArrayList<>(); // weather condition codes

    private final String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

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

        cityAdapter = new CityAdapter(cityList, position -> {
            cityList.remove(position);
            cityTemps.remove(position);
            cityWinds.remove(position);
            cityCodes.remove(position);
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

        btnAddCity.setOnClickListener(v -> {
            String city = editCityInput.getText().toString().trim();
            if (city.isEmpty()) {
                Toast.makeText(this, "Enter a city", Toast.LENGTH_SHORT).show();
            } else {
                new GeocodeTask(city).execute();
            }
        });
    }

    /**
     * Step 1: Geocode city
     */
    private class GeocodeTask extends AsyncTask<Void, Void, double[]> {
        private final String city;

        GeocodeTask(String city) {
            this.city = city;
        }

        @Override
        protected double[] doInBackground(Void... voids) {
            try {
                String urlStr = "https://geocoding-api.open-meteo.com/v1/search?name=" + city + "&count=1";
                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONObject obj = new JSONObject(sb.toString());
                JSONArray results = obj.getJSONArray("results");
                if (results.length() > 0) {
                    JSONObject loc = results.getJSONObject(0);
                    return new double[]{loc.getDouble("latitude"), loc.getDouble("longitude")};
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(double[] latlon) {
            if (latlon != null) {
                new ForecastTask(city, latlon[0], latlon[1]).execute();
            } else {
                Toast.makeText(WeatherDashboardActivity.this, "City not found!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Step 2: Fetch forecast (Temp + Wind + Condition Codes)
     */
    private class ForecastTask extends AsyncTask<Void, Void, Boolean> {
        private final String cityName;
        private final double lat, lon;
        private float[] temps;
        private float[] winds;
        private int[] codes;

        ForecastTask(String cityName, double lat, double lon) {
            this.cityName = cityName;
            this.lat = lat;
            this.lon = lon;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                String urlStr = "https://api.open-meteo.com/v1/forecast?latitude=" + lat +
                        "&longitude=" + lon +
                        "&daily=temperature_2m_max,windspeed_10m_max,weathercode&timezone=auto";
                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONObject obj = new JSONObject(sb.toString());
                JSONObject daily = obj.getJSONObject("daily");
                JSONArray tempsArr = daily.getJSONArray("temperature_2m_max");
                JSONArray windsArr = daily.getJSONArray("windspeed_10m_max");
                JSONArray codesArr = daily.getJSONArray("weathercode");

                temps = new float[7];
                winds = new float[7];
                codes = new int[7];

                for (int i = 0; i < 7; i++) {
                    temps[i] = (float) tempsArr.getDouble(i);
                    winds[i] = (float) windsArr.getDouble(i);
                    codes[i] = codesArr.getInt(i);
                }
                return true;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                cityList.add(new CityWeather(cityName, "Forecast loaded"));
                cityTemps.add(temps);
                cityWinds.add(winds);
                cityCodes.add(codes);
                cityAdapter.notifyItemInserted(cityList.size() - 1);
                loadLineData();
            } else {
                Toast.makeText(WeatherDashboardActivity.this, "Failed to fetch forecast!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showChart(android.view.View chartToShow) {
        lineChart.setVisibility(android.view.View.GONE);
        barChart.setVisibility(android.view.View.GONE);
        pieChart.setVisibility(android.view.View.GONE);
        chartToShow.setVisibility(android.view.View.VISIBLE);
    }

    /**
     * ✅ Line chart = Temperature trend
     */
    private void loadLineData() {
        List<ILineDataSet> dataSets = new ArrayList<>();
        for (int c = 0; c < cityList.size(); c++) {
            List<Entry> entries = new ArrayList<>();
            float[] temps = cityTemps.get(c);
            for (int i = 0; i < temps.length; i++) {
                entries.add(new Entry(i, temps[i]));
            }
            LineDataSet dataSet = new LineDataSet(entries, cityList.get(c).getCityName() + " °C");
            dataSet.setColor(Color.rgb((int) (Math.random() * 255),
                    (int) (Math.random() * 255),
                    (int) (Math.random() * 255)));
            dataSet.setCircleRadius(4f);
            dataSet.setValueTextSize(10f);
            dataSets.add(dataSet);
        }
        lineChart.setData(new LineData(dataSets));

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new ValueFormatter() {
            public String getFormattedValue(float value) {
                int i = (int) value;
                return (i >= 0 && i < days.length) ? days[i] : "";
            }
        });
        lineChart.getDescription().setEnabled(false);
        lineChart.invalidate();
    }

    /**
     * ✅ Bar chart = Temperature vs Wind
     */
    private void loadBarData() {
        if (cityTemps.isEmpty() || cityWinds.isEmpty()) return;

        ArrayList<BarEntry> tempEntries = new ArrayList<>();
        ArrayList<BarEntry> windEntries = new ArrayList<>();

        float[] temps = cityTemps.get(0); // first city
        float[] winds = cityWinds.get(0);

        for (int i = 0; i < temps.length; i++) {
            tempEntries.add(new BarEntry(i, temps[i]));
            windEntries.add(new BarEntry(i, winds[i]));
        }

        BarDataSet tempSet = new BarDataSet(tempEntries, "Temperature (°C)");
        tempSet.setColor(Color.rgb(255, 99, 71));
        tempSet.setValueTextSize(10f);

        BarDataSet windSet = new BarDataSet(windEntries, "Wind (km/h)");
        windSet.setColor(Color.rgb(30, 144, 255));
        windSet.setValueTextSize(10f);

        BarData barData = new BarData(tempSet, windSet);
        float groupSpace = 0.2f;
        float barSpace = 0.05f;
        float barWidth = 0.35f;
        barData.setBarWidth(barWidth);

        barChart.setData(barData);
        barChart.getXAxis().setAxisMinimum(0);
        barChart.getXAxis().setAxisMaximum(temps.length);
        barChart.groupBars(0, groupSpace, barSpace);

        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        barChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < days.length) return days[index];
                return "";
            }
        });

        barChart.getDescription().setEnabled(false);
        barChart.animateY(1000);
        barChart.invalidate();
    }

    /**
     * ✅ Pie chart = Avg Temp per City
     */
    private void loadPieData() {
        List<PieEntry> entries = new ArrayList<>();
        for (int c = 0; c < cityList.size(); c++) {
            float sum = 0;
            for (float t : cityTemps.get(c)) sum += t;
            entries.add(new PieEntry(sum / 7, cityList.get(c).getCityName()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Avg Temp (°C)");

        // 🌈 Use Material Colors for a modern look
        dataSet.setColors(com.github.mikephil.charting.utils.ColorTemplate.MATERIAL_COLORS);

        // Optional: make slices look nicer
        dataSet.setSliceSpace(3f);          // spacing between slices
        dataSet.setSelectionShift(7f);      // pop-out effect on click
        dataSet.setValueTextSize(12f);      // value text size
        dataSet.setValueTextColor(Color.WHITE); // contrast text

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        // Chart styling
        pieChart.setUsePercentValues(false); // show actual values instead of %
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setTransparentCircleRadius(55f);
        pieChart.setHoleRadius(40f);

        pieChart.getDescription().setEnabled(false);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(12f);

        // Add smooth animation
        pieChart.animateY(1000, com.github.mikephil.charting.animation.Easing.EaseInOutQuad);

        pieChart.invalidate();
    }
}
