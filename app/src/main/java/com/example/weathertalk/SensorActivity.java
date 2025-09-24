package com.example.weathertalk;

import android.animation.ValueAnimator;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor stepCounter, accelerometer, magnetometer;

    private ImageView imgCompass, imgSteps;
    private TextView txtDirection, txtSteps;

    private float[] gravity;
    private float[] geomagnetic;
    private int stepCount = 0;
    private float currentAzimuth = 0f; // track last compass angle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        // UI references
        imgCompass = findViewById(R.id.imgCompass);
        imgSteps = findViewById(R.id.imgSteps);
        txtDirection = findViewById(R.id.txtDirection);
        txtSteps = findViewById(R.id.txtSteps);

        // Sensor Manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        if (stepCounter == null) {
            txtSteps.setText(getString(R.string.step_counter_unavailable));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (stepCounter != null)
            sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_UI);
        if (accelerometer != null)
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        if (magnetometer != null)
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int newStepCount = (int) event.values[0];

            // Animate step count increment
            ValueAnimator animator = ValueAnimator.ofInt(stepCount, newStepCount);
            animator.setDuration(500); // half a second
            animator.addUpdateListener(animation -> {
                int animatedValue = (int) animation.getAnimatedValue();
                txtSteps.setText(getString(R.string.steps_taken, animatedValue));
            });
            animator.start();

            stepCount = newStepCount;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values.clone();
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values.clone();
        }

        if (gravity != null && geomagnetic != null) {
            float[] rotationMatrix = new float[9]; // ✅ renamed to avoid conflict with R
            float[] I = new float[9];
            if (SensorManager.getRotationMatrix(rotationMatrix, I, gravity, geomagnetic)) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(rotationMatrix, orientation);

                float azimuth = (float) Math.toDegrees(orientation[0]);
                if (azimuth < 0) azimuth += 360;

                txtDirection.setText(getString(R.string.wind_direction, azimuth));

                // Smooth compass rotation with bounce effect
                RotateAnimation rotate = new RotateAnimation(
                        currentAzimuth,
                        -azimuth,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);

                rotate.setDuration(800); // longer for smooth effect
                rotate.setInterpolator(new OvershootInterpolator()); // bounce effect
                rotate.setFillAfter(true);

                imgCompass.startAnimation(rotate);
                currentAzimuth = -azimuth;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
