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
import android.widget.Toast; // Added for user feedback

import androidx.appcompat.app.AppCompatActivity;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor stepCounterSensor, accelerometerSensor, magneticFieldSensor;

    private ImageView imgCompass, imgSteps;
    private TextView txtDirection, txtSteps;

    // For compass
    private float[] gravityReadings; // Stores accelerometer data
    private float[] geomagneticReadings; // Stores magnetic field data
    private float currentCompassDegrees = 0f; // Stores the current rotation of the compass image

    // For step counter
    private int initialStepCount = -1;  // Stores the total steps at the time the app starts
    private int stepsTakenSession = 0;  // Steps taken since the app started or resumed

    // Constants for sensor delays, can be adjusted
    private static final int SENSOR_DELAY = SensorManager.SENSOR_DELAY_UI; // Suitable for UI updates

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        // --- UI References ---
        imgCompass = findViewById(R.id.imgCompass);
        imgSteps = findViewById(R.id.imgSteps);
        txtDirection = findViewById(R.id.txtDirection);
        txtSteps = findViewById(R.id.txtSteps);

        // --- Sensor Manager Initialization ---
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager == null) {
            Toast.makeText(this, "Sensor services not available!", Toast.LENGTH_LONG).show();
            finish(); // Close activity if no sensor manager
            return;
        }

        // --- Get Sensor Instances ---
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // --- Check Sensor Availability and Provide Feedback ---
        if (stepCounterSensor == null) {
            txtSteps.setText(getString(R.string.step_counter_unavailable));
            imgSteps.setVisibility(TextView.GONE); // Hide icon if sensor is unavailable
            Toast.makeText(this, "Step Counter Sensor Not Found!", Toast.LENGTH_SHORT).show();
        }

        if (accelerometerSensor == null || magneticFieldSensor == null) {
            txtDirection.setText(getString(R.string.compass_unavailable));
            imgCompass.setVisibility(TextView.GONE); // Hide icon if compass sensors are unavailable
            Toast.makeText(this, "Compass Sensors (Accelerometer/Magnetometer) Not Found!", Toast.LENGTH_SHORT).show();
        }

        // Initialize display text
        txtSteps.setText(getString(R.string.steps_taken_placeholder));
        txtDirection.setText(getString(R.string.wind_direction_placeholder));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register listeners for all available sensors
        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SENSOR_DELAY);
        }
        if (accelerometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, SENSOR_DELAY);
        }
        if (magneticFieldSensor != null) {
            sensorManager.registerListener(this, magneticFieldSensor, SENSOR_DELAY);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister all sensor listeners to save battery when the activity is not active
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // --- Step Counter Logic ---
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            // The step counter sensor gives the total number of steps since the device was rebooted.
            // We need to calculate steps taken in *this session*.
            int currentTotalSteps = (int) event.values[0];

            if (initialStepCount == -1) {
                // This is the first reading, set it as our baseline.
                initialStepCount = currentTotalSteps;
            }

            // Calculate steps taken since the app started (or since initialStepCount was set).
            int newSessionSteps = currentTotalSteps - initialStepCount;

            // Only update if steps have actually changed to avoid unnecessary animations/updates
            if (newSessionSteps != stepsTakenSession) {
                // Animate the step count change for a smoother visual update
                ValueAnimator animator = ValueAnimator.ofInt(stepsTakenSession, newSessionSteps);
                animator.setDuration(400); // Animation duration in milliseconds
                animator.addUpdateListener(animation -> {
                    int animatedValue = (int) animation.getAnimatedValue();
                    txtSteps.setText(getString(R.string.steps_taken, animatedValue));
                });
                animator.start();

                stepsTakenSession = newSessionSteps; // Update the session steps
            }
        }

        // --- Compass Logic ---
        // Get readings from Accelerometer and Magnetic Field sensors
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravityReadings = event.values.clone(); // Clone to prevent direct modification
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagneticReadings = event.values.clone(); // Clone to prevent direct modification
        }

        // Once we have both gravity and geomagnetic readings, calculate orientation
        if (gravityReadings != null && geomagneticReadings != null) {
            float[] rotationMatrix = new float[9];
            float[] inclinationMatrix = new float[9];

            // Get the rotation matrix from the sensor readings
            boolean success = SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, gravityReadings, geomagneticReadings);

            if (success) {
                float[] orientationAngles = new float[3];
                // Get the device's orientation in radians (azimuth, pitch, roll)
                SensorManager.getOrientation(rotationMatrix, orientationAngles);

                // Convert azimuth from radians to degrees and normalize to 0-360
                float azimuthInDegrees = (float) Math.toDegrees(orientationAngles[0]);
                if (azimuthInDegrees < 0) {
                    azimuthInDegrees += 360;
                }

                // Update the direction TextView
                txtDirection.setText(getString(R.string.wind_direction, azimuthInDegrees));

                // Animate the compass image rotation
                RotateAnimation rotateAnimation = new RotateAnimation(
                        currentCompassDegrees,        // Start angle
                        -azimuthInDegrees,            // End angle (negative because compass rotates opposite to device)
                        Animation.RELATIVE_TO_SELF, 0.5f, // Pivot X
                        Animation.RELATIVE_TO_SELF, 0.5f); // Pivot Y

                rotateAnimation.setDuration(600);           // Animation duration
                rotateAnimation.setInterpolator(new OvershootInterpolator()); // Smooth interpolation
                rotateAnimation.setFillAfter(true);         // Keep the rotation state after animation

                imgCompass.startAnimation(rotateAnimation);
                currentCompassDegrees = -azimuthInDegrees; // Update the current rotation for the next animation
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not typically used for these sensors, but could be implemented
        // to give feedback on sensor calibration (e.g., if accuracy is low).
    }
}