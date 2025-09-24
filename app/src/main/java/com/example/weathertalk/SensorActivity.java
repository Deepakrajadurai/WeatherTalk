package com.example.weathertalk;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer, magnetometer, stepCounter;
    private float[] gravity, geomagnetic;

    private ImageView imgCompass, imgWindArrow;
    private TextView txtSteps, txtWind;

    private float currentAzimuth = 0f;     // compass rotation
    private float currentWindAngle = 0f;   // wind arrow rotation

    private FusedLocationProviderClient fusedLocationClient;
    private Handler handler = new Handler();

    private int stepCount = 0;
    private boolean stepCounterPresent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 1001);
            }
        }


        imgCompass = findViewById(R.id.imgCompass);
        imgWindArrow = findViewById(R.id.imgWindArrow);
        txtSteps = findViewById(R.id.txtSteps);
        txtWind = findViewById(R.id.txtWind);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        if (stepCounter != null) {
            sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_UI);
        } else {
            txtSteps.setText("Step Counter not available on this device");
        }


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fetchUserLocation();

        // Auto-refresh wind every 5 minutes
        handler.postDelayed(refreshRunnable, 5 * 60 * 1000);
    }

    // Runnable to refresh wind
    private Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            fetchUserLocation();
            handler.postDelayed(this, 5 * 60 * 1000);
        }
    };

    // Get GPS location
    private void fetchUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                fetchWindDirection(location.getLatitude(), location.getLongitude());
            } else {
                Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Fetch wind direction using Open-Meteo API
    private void fetchWindDirection(double lat, double lon) {
        new AsyncTask<Void, Void, Float>() {
            @Override
            protected Float doInBackground(Void... voids) {
                try {
                    String urlStr = "https://api.open-meteo.com/v1/forecast?latitude=" + lat +
                            "&longitude=" + lon +
                            "&current_weather=true";
                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    reader.close();

                    JSONObject obj = new JSONObject(sb.toString());
                    JSONObject current = obj.getJSONObject("current_weather");

                    return (float) current.getDouble("winddirection");
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Float result) {
                if (result != null) {
                    txtWind.setText("Wind Dir: " + result + "°");
                    animateWindArrow(result);
                }
            }
        }.execute();
    }

    // Animate compass
    private void animateCompass(float azimuth) {
        RotateAnimation ra = new RotateAnimation(
                currentAzimuth, -azimuth,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setDuration(500);
        ra.setFillAfter(true);
        imgCompass.startAnimation(ra);

        currentAzimuth = -azimuth;
    }

    // Animate wind arrow
    private void animateWindArrow(float newDirection) {
        RotateAnimation ra = new RotateAnimation(
                currentWindAngle, newDirection,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setDuration(800);
        ra.setFillAfter(true);
        imgWindArrow.startAnimation(ra);

        currentWindAngle = newDirection;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == accelerometer)
            gravity = event.values;
        if (event.sensor == magnetometer)
            geomagnetic = event.values;

        if (gravity != null && geomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                float azimuth = (float) Math.toDegrees(orientation[0]);
                animateCompass(azimuth);
            }
        }

        if (event.sensor == stepCounter && stepCounterPresent) {
            stepCount = (int) event.values[0];
            txtSteps.setText("Steps: " + stepCount);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        if (stepCounterPresent)
            sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(refreshRunnable);
    }
}
